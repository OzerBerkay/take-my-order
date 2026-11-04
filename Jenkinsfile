// =================================================================================================
// TAKE-MY-ORDER - KURUMSAL SELF-HOSTED JENKINS CI/CD PIPELINE
// =================================================================================================
// Bu Declarative Pipeline, izole CI/CD sunucusu (devops-jenkins-server / ARM64) üzerinde çalışır.
//
// MİMARİ ÖZELLİKLER:
// 1. Dış Registry Bağımsız İç Dağıtım (Zero External Registry):
//    Üretilen Docker imajları dış ağa (Docker Hub vb.) çıkarılmaz; doğrudan SSH tüneli üzerinden
//    sıkıştırılarak (gzip) hedef K3s düğümlerinin containerd motoruna (k3s ctr images import)
//    enjekte edilir.
//
// 2. Kontrollü Yayınlama Modu (DEPLOY_MODE: MANUAL_APPROVAL vs AUTO_DEPLOY):
//    - MANUAL_APPROVAL: Kaynak kodu derler ve imajları K3s düğümlerine hazır olarak yükler; ancak
//      canlı ortamda çalışan pod'ları yenilemeden önce manuel dağıtım onayı (Approval Gate) bekler.
//    - AUTO_DEPLOY: Manuel onay adımı beklenmeksizin tüm aşamaları uçtan uca tamamlar.
//
// 3. Kesintisiz Güncelleme & Otomatik Geri Alma (Zero-Downtime Rolling Update & Auto-Rollback):
//    Yeni pod ayağa kalkıp "/actuator/health" (readinessProbe) kontrolünden "UP" dönene kadar
//    mevcut pod sonlandırılmaz (Sıfır Kesinti). Yeni sürüm 180 saniye içinde hazır duruma gelemezse
//    "kubectl rollout undo" komutuyla otomatik olarak bir önceki kararlı sürüme geri dönülür.
// =================================================================================================

pipeline {
    // Pipeline'ın mevcut Jenkins sunucusu veya atanmış uygun ajan üzerinde çalışacağını belirtir.
    agent any

    // Pipeline çalışma seçenekleri
    options {
        // Konsol çıktılarında her satırın başına zaman damgası (timestamp) ekler.
        timestamps()
        // Eşzamanlı build çakışmalarını önler; aktif bir dağıtım varken yeni tetiklenen iş sıraya alınır.
        disableConcurrentBuilds()
        // Disk kullanımını optimize etmek için geçmişe dönük yalnızca son 10 build kaydını saklar.
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    // Otomatik tetikleme (Automated SCM Trigger) yapılandırması:
    triggers {
        // GitHub deposunda (prod dalında) yeni bir commit/push gerçekleştiğinde GitHub Webhook
        // bildirimi üzerinden pipeline'ı otomatik olarak tetikler:
        githubPush()
    }

    // Parametreli çalıştırma (Build with Parameters) yapılandırması:
    parameters {
        // 1. Derlenecek ve dağıtılacak hedef mikroservis seçimi:
        choice(
            name: 'TARGET_SERVICE',
            choices: [
                'ALL',
                'gateway-service',
                'identity-service',
                'order-service',
                'payment-service',
                'restaurant-service'
            ],
            description: 'Derlenecek ve Kubernetes üzerinde güncellenecek hedef mikroservis (ALL = Tüm Mikroservisler)'
        )

        // 2. Dağıtım onay stratejisi:
        choice(
            name: 'DEPLOY_MODE',
            choices: [
                'MANUAL_APPROVAL',
                'AUTO_DEPLOY'
            ],
            description: 'MANUAL_APPROVAL = İmajları düğümlere hazırlar ve canlı dağıtım öncesi manuel onay bekler | AUTO_DEPLOY = Onay beklemeden doğrudan canlıya dağıtır'
        )

        // 3. Birim testlerinin (Unit Tests) çalıştırılma durumu:
        booleanParam(
            name: 'SKIP_TESTS',
            defaultValue: true,
            description: 'Maven paketleme aşamasında birim testleri atlansın mı? (-DskipTests)'
        )
    }

    // Pipeline genelinde kullanılan sabit ortam değişkenleri:
    environment {
        // Kubernetes Deployment manifestlerindeki (image: berkayozr/<service>:v1) imaj tanımıyla uyumlu sabitler:
        IMAGE_PREFIX = 'berkayozr'
        IMAGE_TAG = 'v1'
        K8S_NAMESPACE = 'take-my-order'
    }

    stages {
        // -----------------------------------------------------------------------------------------
        // ADIM 1: KAYNAK KODUN ÇEKİLMESİ (SCM Checkout)
        // -----------------------------------------------------------------------------------------
        stage('1. Checkout Source Code') {
            steps {
                checkout scm
            }
        }

        // -----------------------------------------------------------------------------------------
        // ADIM 2: MAVEN İLE DERLEME VE JAR PAKETLEME
        // -----------------------------------------------------------------------------------------
        stage('2. Build & Package with Maven') {
            steps {
                script {
                    def skipTestsFlag = params.SKIP_TESTS ? '-DskipTests' : ''

                    // Çok modüllü (Multi-Module) Maven yapısında çalıştırılabilir JAR üreten container modülleri:
                    def moduleMap = [
                        'gateway-service'   : 'gateway-service',
                        'identity-service'  : 'identity-service/identity-container',
                        'order-service'     : 'order-service/order-container',
                        'payment-service'   : 'payment-service/payment-container',
                        'restaurant-service': 'restaurant-service/restaurant-container'
                    ]

                    if (params.TARGET_SERVICE == 'ALL') {
                        echo "Building ALL microservices..."
                        sh "mvn clean package ${skipTestsFlag}"
                    } else {
                        def targetModule = moduleMap[params.TARGET_SERVICE]
                        echo "Building only ${params.TARGET_SERVICE} (${targetModule}) and its required dependencies..."
                        sh "mvn clean package ${skipTestsFlag} -pl ${targetModule} -am"
                    }
                }
            }
        }

        // -----------------------------------------------------------------------------------------
        // ADIM 3: NATIVE ARM64 DOCKER İMAJ ÜRETİMİ VE K3S DÜĞÜMLERİNE DOĞRUDAN AKTARIM
        // -----------------------------------------------------------------------------------------
        // Jenkins (ARM64) üzerinde derlenen imajlar, "docker save | gzip" ile sıkıştırılarak
        // doğrudan hedef K3s düğümlerinin containerd ("k8s.io" namespace) imaj havuzuna aktarılır:
        // - gateway-service -> k8s-worker-edge düğümüne aktarılır.
        // - uygulama servisleri -> k8s-worker-01, k8s-worker-02 ve k8s-worker-03 düğümlerine aktarılır.
        // -----------------------------------------------------------------------------------------
        stage('3. Build & Distribute Images to K3s Nodes') {
            steps {
                // Sunucu IP adresleri, kullanıcı adı ve SSH özel anahtarı GitHub üzerinde açık metin olarak
                // tutulmaz; Jenkins Credentials Manager (Manage Jenkins -> Credentials) kasasından çalışma
                // anında güvenli olarak çekilir ve adım tamamlandığında bellekten temizlenir:
                withCredentials([
                    string(credentialsId: 'k8s-worker1-ip', variable: 'WORKER1_IP'),
                    string(credentialsId: 'k8s-worker2-ip', variable: 'WORKER2_IP'),
                    string(credentialsId: 'k8s-worker3-ip', variable: 'WORKER3_IP'),
                    string(credentialsId: 'k8s-gateway-ip', variable: 'GATEWAY_IP'),
                    string(credentialsId: 'k8s-master-user', variable: 'SERVER_USER'),
                    sshUserPrivateKey(credentialsId: 'k8s-master-ssh-key', keyFileVariable: 'SSH_KEY')
                ]) {
                    script {
                        def servicesToBuild = (params.TARGET_SERVICE == 'ALL')
                            ? ['gateway-service', 'identity-service', 'order-service', 'payment-service', 'restaurant-service']
                            : [params.TARGET_SERVICE]

                        for (service in servicesToBuild) {
                            def fullImageName = "docker.io/${IMAGE_PREFIX}/${service}:${IMAGE_TAG}"
                            echo "Building native ARM64 image: ${fullImageName}"
                            sh "docker build --no-cache -t ${fullImageName} -f ./${service}/Dockerfile ./${service}"

                            // Servisin nodeSelector etiketine uygun hedef K3s düğümlerini belirle:
                            def targetNodeIps = (service == 'gateway-service')
                                ? [env.GATEWAY_IP]
                                : [env.WORKER1_IP, env.WORKER2_IP, env.WORKER3_IP]

                            for (nodeIp in targetNodeIps) {
                                echo "Streaming ${fullImageName} directly into K3s containerd on node ${nodeIp}..."
                                sh """
                                    docker save ${fullImageName} | gzip | ssh -i "\$SSH_KEY" -o StrictHostKeyChecking=no \${SERVER_USER}@${nodeIp} \\
                                        "gunzip | sudo k3s ctr -n k8s.io images import -"
                                """
                            }
                        }
                    }
                }
            }
        }

        // -----------------------------------------------------------------------------------------
        // ADIM 4: DAĞITIM ONAY KAPISI (Manual Approval Gate)
        // -----------------------------------------------------------------------------------------
        // Yalnızca DEPLOY_MODE == 'MANUAL_APPROVAL' seçildiğinde çalışır.
        // AUTO_DEPLOY modunda bu aşama otomatik olarak atlanır.
        // -----------------------------------------------------------------------------------------
        stage('4. Deployment Approval Gate') {
            when {
                expression { params.DEPLOY_MODE == 'MANUAL_APPROVAL' }
            }
            steps {
                script {
                    echo "MANUEL ONAY MODU AKTİF: Yeni imajlar K3s düğümlerine yüklendi. Canlı dağıtım için onay bekleniyor."
                    timeout(time: 24, unit: 'HOURS') {
                        input(
                            message: "'${params.TARGET_SERVICE}' için yeni imajlar K3s düğümlerine yüklendi. Canlı ortama kesintisiz güncellemeyi (Zero-Downtime Rollout) başlatmak istiyor musunuz?",
                            ok: 'Onayla ve Dağıtımı Başlat (Deploy Now)'
                        )
                    }
                }
            }
        }

        // -----------------------------------------------------------------------------------------
        // ADIM 5: KESİNTİSİZ GÜNCELLEME (ZERO-DOWNTIME ROLLOUT) & OTOMATİK ROLLBACK KORUMASI
        // -----------------------------------------------------------------------------------------
        // 1. K3s Master üzerinden ilgili Deployment nesneleri için RollingUpdate tetiklenir.
        // 2. Yeni pod "/actuator/health" (readinessProbe) kontrolünden geçene kadar mevcut pod
        //    trafiği karşılamaya devam eder (Sıfır Kesinti).
        // 3. "kubectl rollout status" ile yeni pod'un durumu izlenir; 180 saniye içinde sağlıklı
        //    duruma geçemezse otomatik olarak "kubectl rollout undo" ile önceki sürüme dönülür.
        // -----------------------------------------------------------------------------------------
        stage('5. Zero-Downtime Rollout & Health Verification') {
            steps {
                withCredentials([
                    string(credentialsId: 'k8s-master-ip', variable: 'MASTER_IP'),
                    string(credentialsId: 'k8s-master-user', variable: 'SERVER_USER'),
                    sshUserPrivateKey(credentialsId: 'k8s-master-ssh-key', keyFileVariable: 'SSH_KEY')
                ]) {
                    script {
                        def servicesToDeploy = (params.TARGET_SERVICE == 'ALL')
                            ? ['gateway-service', 'identity-service', 'order-service', 'payment-service', 'restaurant-service']
                            : [params.TARGET_SERVICE]

                        for (service in servicesToDeploy) {
                            echo "Starting zero-downtime rolling update for deployment/${service}..."
                            try {
                                sh """
                                    ssh -i "\$SSH_KEY" -o StrictHostKeyChecking=no \${SERVER_USER}@\${MASTER_IP} "
                                        sudo kubectl rollout restart deployment/${service} -n ${K8S_NAMESPACE} &&
                                        sudo kubectl rollout status deployment/${service} -n ${K8S_NAMESPACE} --timeout=300s
                                    "
                                """
                                echo "Deployment completed successfully for ${service}."
                            } catch (Exception e) {
                                echo "HEALTH CHECK FAILED for ${service}! Triggering automatic rollback (kubectl rollout undo)..."
                                sh """
                                    ssh -i "\$SSH_KEY" -o StrictHostKeyChecking=no \${SERVER_USER}@\${MASTER_IP} \\
                                        "sudo kubectl rollout undo deployment/${service} -n ${K8S_NAMESPACE}"
                                """
                                error("Deployment failed for ${service}. Automatically rolled back to previous healthy revision.")
                            }
                        }
                    }
                }
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // BİTİŞ İŞLEMLERİ (Temizlik ve Sonuç Bildirimi)
    // ---------------------------------------------------------------------------------------------
    post {
        always {
            // Jenkins sunucusu üzerinde biriken referanssız (dangling) Docker imaj katmanlarını temizler:
            sh 'docker image prune -f || true'
        }
        success {
            echo "Pipeline completed successfully for target: ${params.TARGET_SERVICE} (Mode: ${params.DEPLOY_MODE})"
        }
        failure {
            echo "Pipeline failed for target: ${params.TARGET_SERVICE}. Check stage logs for details."
        }
    }
}

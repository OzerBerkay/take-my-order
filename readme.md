!! Be sure docker is running!!
Docker-compose -f common.yml -f zookeeper.yml up → to run Zookeeper.
Echo ruok | nc localhost 2181 or echo ruok | ncat localhost 2181 → Health Check for Zookeeper
docker-compose -f common.yml -f kafka_cluster.yml up → run Kafka 3 broker + manager + schema registry
docker-compose -f common.yml -f init_kafka.yml up → Create Topics (3 partition, 3 replication factor).
Kafka Manager → localhost:9000 → add manually
!! Order is important => Zookeeper → kafka_cluster → init_kafka !!
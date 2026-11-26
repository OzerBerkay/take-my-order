(function () {
  "use strict";

  const MIN_SCALE = 0.35;
  const MAX_SCALE = 5.0;
  const ZOOM_STEP = 0.25;

  let activeModal = null;
  let observer = null;

  function clamp(val, min, max) {
    return Math.min(max, Math.max(min, val));
  }

  function createButton(label, title, svgMarkup, className) {
    const btn = document.createElement("button");
    btn.type = "button";
    btn.className = "mermaid-zoom-btn" + (className ? " " + className : "");
    btn.title = title;
    btn.setAttribute("aria-label", label);
    btn.innerHTML = svgMarkup;
    return btn;
  }

  const ICONS = {
    zoomOut:
      '<svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/><line x1="8" y1="11" x2="14" y2="11"/></svg>',
    zoomIn:
      '<svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/><line x1="11" y1="8" x2="11" y2="14"/><line x1="8" y1="11" x2="14" y2="11"/></svg>',
    reset:
      '<svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5"/></svg>',
    expand:
      '<svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 3 21 3 21 9"/><polyline points="9 21 3 21 3 15"/><line x1="21" y1="3" x2="14" y2="10"/><line x1="3" y1="21" x2="10" y2="14"/></svg>',
    close:
      '<svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>',
  };

  function createPanZoomController(viewport, stage, readoutEl, options) {
    const alwaysAllowDrag = Boolean(options && options.alwaysAllowDrag);
    const wheelWithoutCtrl = Boolean(options && options.wheelWithoutCtrl);

    let scale = 1;
    let tx = 0;
    let ty = 0;
    let isDragging = false;
    let dragStartX = 0;
    let dragStartY = 0;
    let originTx = 0;
    let originTy = 0;

    function updateTransform(smooth) {
      stage.style.transition = smooth ? "transform 0.16s ease-out" : "none";
      stage.style.transform = `translate3d(${tx}px, ${ty}px, 0) scale(${scale})`;
      readoutEl.textContent = `${Math.round(scale * 100)}%`;

      const canPan = alwaysAllowDrag || Math.abs(scale - 1) > 0.01 || Math.abs(tx) > 1 || Math.abs(ty) > 1;
      viewport.classList.toggle("is-pannable", canPan);
    }

    function zoomAtPoint(nextScale, clientX, clientY, smooth) {
      const clamped = clamp(Number(nextScale.toFixed(3)), MIN_SCALE, MAX_SCALE);
      if (clamped === scale) return;

      const rect = viewport.getBoundingClientRect();
      const vx = rect.left + rect.width / 2;
      const vy = rect.top + rect.height / 2;
      const dx = (clientX ?? vx) - vx;
      const dy = (clientY ?? vy) - vy;

      const ratio = clamped / scale;
      tx = dx - ratio * (dx - tx);
      ty = dy - ratio * (dy - ty);
      scale = clamped;

      if (Math.abs(scale - 1) < 0.01 && !alwaysAllowDrag) {
        scale = 1;
        tx = 0;
        ty = 0;
      }

      updateTransform(smooth);
    }

    function reset(smooth) {
      scale = 1;
      tx = 0;
      ty = 0;
      updateTransform(smooth !== false);
    }

    viewport.addEventListener(
      "wheel",
      function (e) {
        if (!wheelWithoutCtrl && !e.ctrlKey && !e.metaKey) {
          return;
        }
        e.preventDefault();
        const factor = e.deltaY < 0 ? 1.12 : 1 / 1.12;
        zoomAtPoint(scale * factor, e.clientX, e.clientY, false);
      },
      { passive: false }
    );

    viewport.addEventListener("pointerdown", function (e) {
      if (e.button !== 0) return;
      if (!alwaysAllowDrag && Math.abs(scale - 1) <= 0.01 && Math.abs(tx) <= 1 && Math.abs(ty) <= 1) {
        return;
      }
      isDragging = true;
      dragStartX = e.clientX;
      dragStartY = e.clientY;
      originTx = tx;
      originTy = ty;
      viewport.classList.add("is-dragging");
      viewport.setPointerCapture(e.pointerId);
    });

    viewport.addEventListener("pointermove", function (e) {
      if (!isDragging) return;
      tx = originTx + (e.clientX - dragStartX);
      ty = originTy + (e.clientY - dragStartY);
      updateTransform(false);
    });

    function stopDragging(e) {
      if (!isDragging) return;
      isDragging = false;
      viewport.classList.remove("is-dragging");
      if (e && e.pointerId !== undefined && viewport.hasPointerCapture(e.pointerId)) {
        viewport.releasePointerCapture(e.pointerId);
      }
    }

    viewport.addEventListener("pointerup", stopDragging);
    viewport.addEventListener("pointercancel", stopDragging);

    viewport.addEventListener("dblclick", function (e) {
      e.preventDefault();
      if (Math.abs(scale - 1) > 0.05) {
        reset(true);
      } else {
        zoomAtPoint(1.85, e.clientX, e.clientY, true);
      }
    });

    updateTransform(false);

    return {
      zoomIn: () => zoomAtPoint(scale + ZOOM_STEP, null, null, true),
      zoomOut: () => zoomAtPoint(scale - ZOOM_STEP, null, null, true),
      reset: () => reset(true),
    };
  }

  function createToolbarElements(options) {
    const toolbar = document.createElement("div");
    toolbar.className = "mermaid-zoom-toolbar";

    const btnOut = createButton("Zoom out", "Zoom out", ICONS.zoomOut);
    const readout = document.createElement("button");
    readout.type = "button";
    readout.className = "mermaid-zoom-readout";
    readout.title = "Reset zoom to 100%";
    readout.textContent = "100%";

    const btnIn = createButton("Zoom in", "Zoom in", ICONS.zoomIn);
    const btnReset = createButton("Reset view", "Reset view", ICONS.reset);

    toolbar.appendChild(btnOut);
    toolbar.appendChild(readout);
    toolbar.appendChild(btnIn);
    toolbar.appendChild(btnReset);

    let btnExpand = null;
    if (options && options.showExpand) {
      const divider = document.createElement("span");
      divider.className = "mermaid-zoom-divider";
      btnExpand = createButton("Fullscreen", "Inspect in fullscreen", ICONS.expand, "is-accent");
      toolbar.appendChild(divider);
      toolbar.appendChild(btnExpand);
    }

    let btnClose = null;
    if (options && options.showClose) {
      const divider = document.createElement("span");
      divider.className = "mermaid-zoom-divider";
      btnClose = createButton("Close fullscreen", "Close (Esc)", ICONS.close, "is-close");
      toolbar.appendChild(divider);
      toolbar.appendChild(btnClose);
    }

    return { toolbar, btnOut, readout, btnIn, btnReset, btnExpand, btnClose };
  }

  function closeModal() {
    if (!activeModal) return;
    const { modal, mermaidEl, restoreParent, restoreNextSibling, onKeyDown } = activeModal;

    document.removeEventListener("keydown", onKeyDown);
    document.body.classList.remove("mermaid-modal-open");

    mermaidEl.classList.remove("is-modal-diagram");
    if (restoreNextSibling && restoreNextSibling.parentNode === restoreParent) {
      restoreParent.insertBefore(mermaidEl, restoreNextSibling);
    } else {
      restoreParent.appendChild(mermaidEl);
    }

    modal.remove();
    activeModal = null;
  }

  function openModal(mermaidEl) {
    if (activeModal) {
      closeModal();
    }

    const restoreParent = mermaidEl.parentNode;
    const restoreNextSibling = mermaidEl.nextSibling;

    const modal = document.createElement("div");
    modal.className = "mermaid-zoom-modal";
    modal.setAttribute("role", "dialog");
    modal.setAttribute("aria-modal", "true");
    modal.setAttribute("aria-label", "Interactive architecture diagram");

    const dialog = document.createElement("div");
    dialog.className = "mermaid-zoom-modal-dialog";

    const header = document.createElement("div");
    header.className = "mermaid-zoom-modal-header";

    const title = document.createElement("span");
    title.className = "mermaid-zoom-modal-title";
    title.textContent = "Diagram Inspector · Scroll to zoom · Drag to pan · Esc to close";

    const viewport = document.createElement("div");
    viewport.className = "mermaid-zoom-viewport is-fullscreen";

    const stage = document.createElement("div");
    stage.className = "mermaid-zoom-stage";

    mermaidEl.classList.add("is-modal-diagram");
    stage.appendChild(mermaidEl);
    viewport.appendChild(stage);

    const { toolbar, btnOut, readout, btnIn, btnReset, btnClose } = createToolbarElements({
      showClose: true,
    });

    const controller = createPanZoomController(viewport, stage, readout, {
      alwaysAllowDrag: true,
      wheelWithoutCtrl: true,
    });

    btnIn.addEventListener("click", controller.zoomIn);
    btnOut.addEventListener("click", controller.zoomOut);
    btnReset.addEventListener("click", controller.reset);
    readout.addEventListener("click", controller.reset);
    btnClose.addEventListener("click", closeModal);

    header.appendChild(title);
    header.appendChild(toolbar);
    dialog.appendChild(header);
    dialog.appendChild(viewport);
    modal.appendChild(dialog);

    modal.addEventListener("click", function (e) {
      if (e.target === modal) {
        closeModal();
      }
    });

    function onKeyDown(e) {
      if (e.key === "Escape") {
        closeModal();
      } else if (e.key === "+" || e.key === "=") {
        controller.zoomIn();
      } else if (e.key === "-") {
        controller.zoomOut();
      } else if (e.key === "0") {
        controller.reset();
      }
    }

    document.addEventListener("keydown", onKeyDown);
    document.body.classList.add("mermaid-modal-open");
    document.body.appendChild(modal);

    activeModal = {
      modal,
      mermaidEl,
      restoreParent,
      restoreNextSibling,
      onKeyDown,
    };
  }

  function enhanceMermaidElement(mermaidEl) {
    if (!mermaidEl || mermaidEl.tagName !== "DIV" || mermaidEl.dataset.zoomAttached === "1") {
      return;
    }
    if (mermaidEl.closest(".mermaid-zoom-card") || mermaidEl.closest(".mermaid-zoom-modal")) {
      return;
    }

    mermaidEl.dataset.zoomAttached = "1";

    const card = document.createElement("div");
    card.className = "mermaid-zoom-card";

    const viewport = document.createElement("div");
    viewport.className = "mermaid-zoom-viewport";

    const stage = document.createElement("div");
    stage.className = "mermaid-zoom-stage";

    const parent = mermaidEl.parentNode;
    parent.insertBefore(card, mermaidEl);
    stage.appendChild(mermaidEl);
    viewport.appendChild(stage);

    const { toolbar, btnOut, readout, btnIn, btnReset, btnExpand } = createToolbarElements({
      showExpand: true,
    });

    const controller = createPanZoomController(viewport, stage, readout, {
      alwaysAllowDrag: false,
      wheelWithoutCtrl: false,
    });

    btnOut.addEventListener("click", controller.zoomOut);
    btnIn.addEventListener("click", controller.zoomIn);
    btnReset.addEventListener("click", controller.reset);
    readout.addEventListener("click", controller.reset);
    btnExpand.addEventListener("click", () => openModal(mermaidEl));

    const hint = document.createElement("div");
    hint.className = "mermaid-zoom-hint";
    hint.textContent = "Ctrl + Scroll to zoom · Drag when zoomed · Fullscreen to inspect";

    card.appendChild(toolbar);
    card.appendChild(viewport);
    card.appendChild(hint);
  }

  function scanMermaidDiagrams() {
    const nodes = document.querySelectorAll("div.mermaid");
    nodes.forEach(enhanceMermaidElement);
  }

  function init() {
    closeModal();
    scanMermaidDiagrams();

    if (!observer && document.body) {
      observer = new MutationObserver((mutations) => {
        for (const mutation of mutations) {
          for (const node of mutation.addedNodes) {
            if (!(node instanceof Element)) continue;
            if (node.matches("div.mermaid")) {
              enhanceMermaidElement(node);
            } else {
              const nested = node.querySelectorAll("div.mermaid");
              if (nested.length > 0) {
                nested.forEach(enhanceMermaidElement);
              }
            }
          }
        }
      });

      observer.observe(document.body, { childList: true, subtree: true });
    }
  }

  if (typeof document$ !== "undefined" && document$.subscribe) {
    document$.subscribe(init);
  } else if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }
})();

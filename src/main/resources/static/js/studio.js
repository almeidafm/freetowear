'use strict';
(() => {
  class ShirtStudio {
    constructor(container, state, callbacks = {}) {
      this.container = container;
      this.state = state;
      this.callbacks = callbacks;
      this.alive = true;
      this.serial = 0;
      this.meshes = [];
      this.decals = { front: [], back: [] };
      this.textures = {};
      this.artSerial = { front: 0, back: 0 };
      this.raycaster = new THREE.Raycaster();
      this.scene = new THREE.Scene();
      this.camera = new THREE.PerspectiveCamera(38, 1, .01, 100);
      this.renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true, preserveDrawingBuffer: true });
      this.renderer.setPixelRatio(Math.min(devicePixelRatio || 1, 2));
      this.renderer.outputEncoding = THREE.sRGBEncoding;
      this.renderer.shadowMap.enabled = true;
      this.renderer.shadowMap.type = THREE.PCFSoftShadowMap;
      this.renderer.domElement.setAttribute('aria-label', 'Modelo 3D da camiseta. Use os botões Frente, Costas e Enquadrar para mudar a visão.');
      container.prepend(this.renderer.domElement);
      this.controls = new THREE.OrbitControls(this.camera, this.renderer.domElement);
      this.controls.enableDamping = true;
      this.controls.dampingFactor = .08;
      this.controls.enablePan = false;
      this.controls.minPolarAngle = .15;
      this.controls.maxPolarAngle = Math.PI - .15;
      this.scene.add(new THREE.HemisphereLight(0xffffff, 0x8694a6, .85));
      const key = new THREE.DirectionalLight(0xffffff, .75);
      key.position.set(3, 5, 5);
      key.castShadow = true;
      key.shadow.mapSize.set(1024, 1024);
      key.shadow.camera.left = key.shadow.camera.bottom = -4;
      key.shadow.camera.right = key.shadow.camera.top = 4;
      key.shadow.normalBias = .015;
      this.scene.add(key);
      const fill = new THREE.DirectionalLight(0xe8f5ff, .45);
      fill.position.set(-4, 2, -4);
      this.scene.add(fill);
      this.floor = new THREE.Mesh(new THREE.PlaneGeometry(30, 30), new THREE.ShadowMaterial({ opacity: .11 }));
      this.floor.rotation.x = -Math.PI / 2;
      this.floor.receiveShadow = true;
      this.scene.add(this.floor);
      this.pointerDown = event => {
        if (!this.placement || !this.model || event.button !== 0) return;
        this.dragging = true;
        this.renderer.domElement.setPointerCapture(event.pointerId);
        this.placeFromPointer(event);
      };
      this.pointerMove = event => { if (this.dragging) this.placeFromPointer(event); };
      this.pointerUp = () => { this.dragging = false; };
      this.renderer.domElement.addEventListener('pointerdown', this.pointerDown);
      this.renderer.domElement.addEventListener('pointermove', this.pointerMove);
      this.renderer.domElement.addEventListener('pointerup', this.pointerUp);
      this.renderer.domElement.addEventListener('pointercancel', this.pointerUp);
      this.contextLost = event => { event.preventDefault(); this.status('error', 'A visualização 3D foi interrompida. Recarregue a página para continuar.'); };
      this.renderer.domElement.addEventListener('webglcontextlost', this.contextLost);
      this.resizeObserver = new ResizeObserver(() => this.resize());
      this.resizeObserver.observe(container);
      this.resize();
      this.animate = () => {
        if (!this.alive) return;
        this.frame = requestAnimationFrame(this.animate);
        this.controls.update();
        this.renderer.render(this.scene, this.camera);
      };
      this.animate();
      this.loadDefault();
    }
    status(kind, message) { if (this.alive) this.callbacks.status?.(kind, message); }
    resize() {
      const w = this.container.clientWidth, h = this.container.clientHeight;
      if (!w || !h || !this.alive) return;
      this.camera.aspect = w / h;
      this.camera.updateProjectionMatrix();
      this.renderer.setSize(w, h);
      if (this.model) this.fit(this.currentView || 'front');
    }
    async loadDefault() {
      const serial = ++this.serial;
      this.abort?.abort();
      this.abort = new AbortController();
      this.status('loading', 'Carregando modelo 3D…');
      if (!this.alive || serial !== this.serial) return;
      this.loadDemo();
    }
    acceptOBJ(source, name) {
      if (!/^\s*v\s+[-+.\d]/m.test(source) || !/^\s*f\s+/m.test(source)) throw new Error('invalid');
      const object = new THREE.OBJLoader().parse(source);
      this.installModel(object, name, false);
    }
    loadDemo() {
      this.abort?.abort();
      ++this.serial;
      const shape = new THREE.Shape();
      shape.moveTo(-.35, 1.05); shape.lineTo(-.72, .94); shape.lineTo(-1.22, .53);
      shape.lineTo(-.91, .12); shape.lineTo(-.65, .34); shape.lineTo(-.62, -1.0);
      shape.quadraticCurveTo(0, -1.08, .62, -1.0); shape.lineTo(.65, .34);
      shape.lineTo(.91, .12); shape.lineTo(1.22, .53); shape.lineTo(.72, .94);
      shape.lineTo(.35, 1.05); shape.quadraticCurveTo(.25, .72, 0, .71);
      shape.quadraticCurveTo(-.25, .72, -.35, 1.05);
      const geometry = new THREE.ExtrudeGeometry(shape, { depth: .16, bevelEnabled: true, bevelThickness: .045, bevelSize: .035, bevelSegments: 3, steps: 1, curveSegments: 24 });
      const object = new THREE.Group();
      object.add(new THREE.Mesh(geometry, new THREE.MeshStandardMaterial()));
      this.installModel(object, 'Demonstração — substitua pelo seu OBJ', true);
    }
    installModel(object, name, demo) {
      object.updateMatrixWorld(true);
      const meshes = [];
      const material = new THREE.MeshStandardMaterial({ color: this.state.color, roughness: .9, metalness: 0, side: THREE.DoubleSide });
      const newGroup = new THREE.Group();
      object.traverse(child => {
        if (child.isMesh && child.geometry?.attributes.position?.count >= 3) {
          const geometry = child.geometry.clone();
          geometry.applyMatrix4(child.matrixWorld);
          if (!geometry.attributes.normal) geometry.computeVertexNormals();
          const mesh = new THREE.Mesh(geometry, material);
          mesh.castShadow = true;
          newGroup.add(mesh); meshes.push(mesh);
        }
      });
      const sourceMaterials = new Set();
      object.traverse(child => {
        child.geometry?.dispose();
        if (Array.isArray(child.material)) child.material.forEach(m => sourceMaterials.add(m));
        else if (child.material) sourceMaterials.add(child.material);
      });
      sourceMaterials.forEach(m => m.dispose());
      const box = new THREE.Box3().setFromObject(newGroup);
      const size = box.getSize(new THREE.Vector3());
      if (!meshes.length || ![size.x, size.y, size.z].every(Number.isFinite) || size.length() < 1e-10) {
        meshes.forEach(m => m.geometry.dispose()); material.dispose(); throw new Error('invalid');
      }
      this.releaseModel();
      this.model = newGroup;
      this.material = material;
      this.meshes = meshes;
      this.isDemo = demo;
      this.modelName = name;
      this.scene.add(newGroup);
      const rotation = this.state.orientation || [0, 0, 0];
      newGroup.rotation.set(...rotation);
      this.normalize();
      for (const side of ['front', 'back']) if (this.state[side].dataUrl) this.setArtwork(side, this.state[side].dataUrl);
      this.status(demo ? 'demo' : 'ready', name);
    }
    normalize() {
      this.model.scale.setScalar(1);
      this.model.position.set(0, 0, 0);
      this.model.updateMatrixWorld(true);
      const box = new THREE.Box3().setFromObject(this.model);
      const size = box.getSize(new THREE.Vector3());
      const scale = 2.7 / Math.max(size.x, size.y, size.z);
      this.model.scale.setScalar(scale);
      this.model.position.copy(box.getCenter(new THREE.Vector3())).multiplyScalar(-scale);
      this.model.updateMatrixWorld(true);
      this.bounds = new THREE.Box3().setFromObject(this.model);
      this.size = this.bounds.getSize(new THREE.Vector3());
      this.floor.position.y = this.bounds.min.y - .12;
      this.fit(this.state.side || 'front');
      this.updateDecals();
    }
    fit(view = 'front') {
      if (!this.model) return;
      this.currentView = view;
      const v = THREE.MathUtils.degToRad(this.camera.fov);
      const d = Math.max(this.size.y / (2 * Math.tan(v / 2)), this.size.x / (2 * Math.tan(v / 2) * this.camera.aspect)) * 1.38 + this.size.z;
      this.camera.position.set(0, 0, (view === 'back' ? -1 : 1) * d);
      this.camera.near = .01;
      this.camera.far = Math.max(100, d * 10);
      this.camera.updateProjectionMatrix();
      this.controls.target.set(0, 0, 0);
      this.controls.minDistance = Math.max(.6, this.size.z + .3);
      this.controls.maxDistance = Math.max(12, d * 2);
      this.controls.update();
    }
    setColor(color) { if (this.material) this.material.color.set(color); }
    setPlacement(enabled) {
      this.placement = enabled;
      this.controls.enabled = !enabled;
      this.renderer.domElement.style.cursor = enabled ? 'crosshair' : 'grab';
      if (enabled) this.fit(this.state.side);
    }
    placeFromPointer(event) {
      const rect = this.renderer.domElement.getBoundingClientRect();
      const pointer = new THREE.Vector2((event.clientX - rect.left) / rect.width * 2 - 1, -(event.clientY - rect.top) / rect.height * 2 + 1);
      this.raycaster.setFromCamera(pointer, this.camera);
      const hit = this.raycaster.intersectObjects(this.meshes, false)[0];
      if (!hit) return;
      const side = this.state.side;
      this.state[side].x = THREE.MathUtils.clamp(hit.point.x / this.size.x * 100 * (side === 'back' ? -1 : 1), -48, 48);
      this.state[side].y = THREE.MathUtils.clamp(hit.point.y / this.size.y * 100, -48, 48);
      this.updateDecal(side);
      this.callbacks.position?.();
    }
    async setArtwork(side, dataUrl) {
      const serial = ++this.artSerial[side];
      this.clearDecal(side);
      this.textures[side]?.dispose();
      delete this.textures[side];
      if (!dataUrl) return;
      const image = new Image();
      image.onload = () => {
        if (!this.alive || serial !== this.artSerial[side]) return;
        const texture = new THREE.Texture(image);
        texture.encoding = THREE.sRGBEncoding;
        texture.anisotropy = Math.min(8, this.renderer.capabilities.getMaxAnisotropy());
        texture.needsUpdate = true;
        this.textures[side] = texture;
        this.state[side].aspect = image.width / image.height;
        this.updateDecal(side);
      };
      image.onerror = () => { if (this.alive && serial === this.artSerial[side]) this.callbacks.notice?.('Não foi possível abrir a estampa.'); };
      image.src = dataUrl;
    }
    clearDecal(side) {
      for (const mesh of this.decals[side]) { this.scene.remove(mesh); mesh.geometry.dispose(); mesh.material.dispose(); }
      this.decals[side] = [];
    }
    updateDecals() { this.updateDecal('front'); this.updateDecal('back'); }
    updateDecal(side) {
      this.clearDecal(side);
      if (!this.model || !this.textures[side]) return;
      const art = this.state[side];
      const sign = side === 'back' ? -1 : 1;
      const x = art.x / 100 * this.size.x * sign;
      const y = art.y / 100 * this.size.y;
      const z = sign > 0 ? this.bounds.max.z + 1 : this.bounds.min.z - 1;
      this.raycaster.set(new THREE.Vector3(x, y, z), new THREE.Vector3(0, 0, -sign));
      const hit = this.raycaster.intersectObjects(this.meshes, false)[0];
      if (!hit) { this.callbacks.artStatus?.(side, 'Posição fora da camisa. Mova a estampa para a malha.'); return; }
      const width = art.width / 100 * this.size.x;
      const depth = Math.max(this.size.z * .72, .04);
      const projectionSize = new THREE.Vector3(width, width / (art.aspect || 1), depth);
      const orientation = new THREE.Euler(0, side === 'back' ? Math.PI : 0, THREE.MathUtils.degToRad(-art.rotation), 'XYZ');
      for (const mesh of this.meshes) {
        const geometry = new THREE.DecalGeometry(mesh, hit.point, orientation, projectionSize);
        // Exclui faces voltadas para o lado oposto para a arte não atravessar a camisa.
        const normals = geometry.getAttribute('normal');
        const indices = [];
        for (let i = 0; i < normals.count; i += 3) {
          if ((normals.getZ(i) + normals.getZ(i + 1) + normals.getZ(i + 2)) * sign > .12) indices.push(i, i + 1, i + 2);
        }
        if (!indices.length) { geometry.dispose(); continue; }
        geometry.setIndex(indices);
        const material = new THREE.MeshStandardMaterial({ map: this.textures[side], color: 0xffffff, roughness: .95, metalness: 0, transparent: true, alphaTest: .015, depthWrite: false, polygonOffset: true, polygonOffsetFactor: -4, side: THREE.DoubleSide });
        const decal = new THREE.Mesh(geometry, material);
        decal.renderOrder = 2;
        this.scene.add(decal); this.decals[side].push(decal);
      }
      this.callbacks.artStatus?.(side, this.decals[side].length ? '' : 'A superfície está invertida ou não recebe a projeção neste ponto. Confira a orientação do OBJ.');
    }
    releaseModel() {
      this.clearDecal('front'); this.clearDecal('back');
      this.meshes.forEach(mesh => mesh.geometry.dispose());
      this.meshes = [];
      this.material?.dispose();
      if (this.model) this.scene.remove(this.model);
      this.model = null;
    }
    dispose() {
      if (!this.alive) return;
      this.alive = false; ++this.serial;
      this.abort?.abort(); cancelAnimationFrame(this.frame);
      this.resizeObserver.disconnect(); this.controls.dispose();
      const canvas = this.renderer.domElement;
      canvas.removeEventListener('pointerdown', this.pointerDown);
      canvas.removeEventListener('pointermove', this.pointerMove);
      canvas.removeEventListener('pointerup', this.pointerUp);
      canvas.removeEventListener('pointercancel', this.pointerUp);
      canvas.removeEventListener('webglcontextlost', this.contextLost);
      this.releaseModel();
      Object.values(this.textures).forEach(texture => texture.dispose());
      this.floor.geometry.dispose(); this.floor.material.dispose();
      this.scene.traverse(item => { if (item.isLight && item.shadow) item.shadow.dispose(); });
      this.renderer.dispose(); this.renderer.forceContextLoss(); canvas.remove();
    }
  }
  window.ShirtStudio = ShirtStudio;
})();

document.addEventListener('DOMContentLoaded', () => {
  const container = document.getElementById('canvas-container');
  const overlay = document.getElementById('model-overlay');
  const toast = document.getElementById('toast');
  const state = {
    color: '#ffffff',
    side: 'front',
    orientation: [0, 0, 0],
    front: { dataUrl: null, x: 0, y: 0, width: 42, rotation: 0, aspect: 1 },
    back: { dataUrl: null, x: 0, y: 0, width: 42, rotation: 0, aspect: 1 }
  };
  const artworkFiles = {
    jurassico: 'jurassico.png',
    cafe: 'cafe.png',
    cosmos: 'cosmos.png',
    game: 'game.png'
  };
  const notice = message => {
    toast.textContent = message;
    toast.hidden = false;
    window.setTimeout(() => { toast.hidden = true; }, 3500);
  };
  const studio = new ShirtStudio(container, state, {
    status: (kind, message) => {
      overlay.hidden = kind === 'ready' || kind === 'demo';
      if (kind === 'missing' || kind === 'error') overlay.hidden = false;
    },
    notice
  });

  document.querySelectorAll('[data-side]').forEach(button => button.addEventListener('click', () => {
    state.side = button.dataset.side;
    document.querySelectorAll('[data-side]').forEach(item => {
      const active = item === button;
      item.classList.toggle('active', active);
      item.setAttribute('aria-pressed', active);
    });
    studio.fit(state.side);
  }));
  document.querySelectorAll('[data-camera]').forEach(button =>
    button.addEventListener('click', () => studio.fit(button.dataset.camera)));
  document.getElementById('fit-camera').addEventListener('click', () => studio.fit(state.side));
  document.getElementById('art-file').addEventListener('change', event => {
    const file = event.target.files[0];
    if (!file) return;
    const reader = new FileReader();
    reader.onload = () => {
      state[state.side].dataUrl = reader.result;
      document.getElementById('art-name').textContent = file.name;
      document.querySelectorAll('[data-range], #reset-art').forEach(control => control.disabled = false);
      studio.setArtwork(state.side, reader.result);
    };
    reader.readAsDataURL(file);
  });
  const selectedArt = new URLSearchParams(window.location.search).get('art');
  const selectedArtFile = artworkFiles[selectedArt];
  if (selectedArtFile) {
    const dataUrl = `/img/${selectedArtFile}`;
    state.front.dataUrl = dataUrl;
    state.side = 'front';
    document.getElementById('art-name').textContent = selectedArtFile;
    document.querySelectorAll('[data-range], #reset-art').forEach(control => control.disabled = false);
    studio.setArtwork('front', dataUrl);
  }
  document.querySelectorAll('[data-range]').forEach(input => input.addEventListener('input', () => {
    const side = state[state.side];
    const key = input.dataset.range;
    side[key] = Number(input.value);
    document.getElementById(`value-${key}`).textContent = `${input.value}${key === 'rotation' ? '°' : '%'}`;
    studio.updateDecal(state.side);
  }));
  document.getElementById('reset-art').addEventListener('click', () => {
    const side = state[state.side];
    Object.assign(side, { x: 0, y: 0, width: 44, rotation: 0 });
    document.querySelectorAll('[data-range]').forEach(input => {
      input.value = side[input.dataset.range];
      document.getElementById(`value-${input.dataset.range}`).textContent = `${input.value}${input.dataset.range === 'rotation' ? '°' : '%'}`;
    });
    studio.updateDecal(state.side);
  });
});
import * as THREE from 'three';
import {GLTFLoader} from 'three/examples/jsm/loaders/GLTFLoader.js';
import {RoomEnvironment} from 'three/examples/jsm/environments/RoomEnvironment.js';

// ── 장면 ──────────────────────────────────────────────
const canvas = document.getElementById('c');
const renderer = new THREE.WebGLRenderer({canvas, antialias: true, alpha: true, preserveDrawingBuffer: !!window.SF_SHOT});
renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2));
renderer.outputColorSpace = THREE.SRGBColorSpace;
renderer.toneMapping = THREE.ACESFilmicToneMapping;
renderer.toneMappingExposure = 1.05;
renderer.shadowMap.enabled = true;
renderer.shadowMap.type = THREE.PCFSoftShadowMap;
renderer.setClearColor(0x000000, 0);

const scene = new THREE.Scene();
{ const pm = new THREE.PMREMGenerator(renderer); scene.environment = pm.fromScene(new RoomEnvironment(), 0.04).texture; scene.environmentIntensity = 0.35; }
const camera = new THREE.PerspectiveCamera(28, 1, 0.05, 50);

scene.add(new THREE.HemisphereLight(0xffffff, 0x8d7f74, 0.9));
const key = new THREE.DirectionalLight(0xffffff, 2.2);
key.position.set(0.9, 4.0, 2.2);
key.castShadow = true;
key.shadow.mapSize.set(1024, 1024);
key.shadow.camera.left = -1.6; key.shadow.camera.right = 1.6;
key.shadow.camera.top = 1.6; key.shadow.camera.bottom = -1.6;
key.shadow.camera.near = 0.5; key.shadow.camera.far = 8;
key.shadow.bias = -0.0004; key.shadow.normalBias = 0.02;
key.shadow.radius = 4;
scene.add(key); scene.add(key.target);
const rim = new THREE.DirectionalLight(0xdcd6ff, 1.1);
rim.position.set(-2.2, 2.0, -2.5);
scene.add(rim);

// 바닥: 부드러운 원형 바닥 + 그림자
function discTexture() {
  const c = document.createElement('canvas'); c.width = c.height = 256;
  const g = c.getContext('2d');
  const grd = g.createRadialGradient(128, 128, 10, 128, 128, 128);
  grd.addColorStop(0, 'rgba(120,110,160,0.22)');
  grd.addColorStop(0.6, 'rgba(120,110,160,0.10)');
  grd.addColorStop(1, 'rgba(120,110,160,0)');
  g.fillStyle = grd; g.fillRect(0, 0, 256, 256);
  const t = new THREE.CanvasTexture(c); t.colorSpace = THREE.SRGBColorSpace; return t;
}
const disc = new THREE.Mesh(new THREE.PlaneGeometry(3.2, 3.2), new THREE.MeshBasicMaterial({map: discTexture(), transparent: true, depthWrite: false}));
disc.rotation.x = -Math.PI / 2; disc.position.y = 0.001;
scene.add(disc);
const shadowFloor = new THREE.Mesh(new THREE.PlaneGeometry(8, 8), new THREE.ShadowMaterial({opacity: 0.22}));
shadowFloor.rotation.x = -Math.PI / 2; shadowFloor.position.y = 0.002; shadowFloor.receiveShadow = true;
scene.add(shadowFloor);

// ── 관절 번호 ─────────────────────────────────────────
const JN = ['HEAD','NECK','CHEST','PELVIS','L_SHOULDER','L_ELBOW','L_WRIST','R_SHOULDER','R_ELBOW','R_WRIST',
  'L_HIP','L_KNEE','L_ANKLE','L_TOE','R_HIP','R_KNEE','R_ANKLE','R_TOE','FACE','L_HAND','R_HAND'];
const J = Object.fromEntries(JN.map((n, i) => [n, i]));

// ── 모델 ──────────────────────────────────────────────
let model = null, character = null, skinned = [], B = {}, rest = {};
const V = (a) => new THREE.Vector3(a[0], a[1], a[2]);
const tmpV = new THREE.Vector3(), tmpQ = new THREE.Quaternion(), tmpM = new THREE.Matrix4();

function basis(primary, secondary) {
  // primary: 뼈 방향, secondary: 그와 수직으로 맞출 보조축 → 회전
  const a = primary.clone().normalize();
  const h = secondary.clone().addScaledVector(a, -secondary.dot(a));
  if (h.lengthSq() < 1e-8) h.set(a.y, -a.x, 0.3).addScaledVector(a, -a.dot(tmpV.set(a.y, -a.x, 0.3)));
  h.normalize();
  const c = new THREE.Vector3().crossVectors(a, h);
  return new THREE.Quaternion().setFromRotationMatrix(new THREE.Matrix4().makeBasis(a, h, c));
}
// 몸통용: Y(위)를 기준으로, X(왼쪽)를 맞춰요. 기준 자세(정면, 똑바로)에서는 단위 회전.
function bodyBasis(up, left) {
  const y = up.clone().normalize();
  const x = left.clone().addScaledVector(y, -left.dot(y)).normalize();
  const z = new THREE.Vector3().crossVectors(x, y);
  return new THREE.Quaternion().setFromRotationMatrix(new THREE.Matrix4().makeBasis(x, y, z));
}
function faceBasis(fwd, left) {
  const z = fwd.clone().normalize();
  const x = left.clone().addScaledVector(z, -left.dot(z)).normalize();
  const y = new THREE.Vector3().crossVectors(z, x);
  return new THREE.Quaternion().setFromRotationMatrix(new THREE.Matrix4().makeBasis(x, y, z));
}

const BONES = {
  hips: 'Hips', spine: 'Spine', spine1: 'Spine1', spine2: 'Spine2', neck: 'Neck', head: 'Head',
  lClav: 'LeftShoulder', lArm: 'LeftArm', lFore: 'LeftForeArm', lHand: 'LeftHand',
  rClav: 'RightShoulder', rArm: 'RightArm', rFore: 'RightForeArm', rHand: 'RightHand',
  lUp: 'LeftUpLeg', lLeg: 'LeftLeg', lFoot: 'LeftFoot', lToe: 'LeftToeBase',
  rUp: 'RightUpLeg', rLeg: 'RightLeg', rFoot: 'RightFoot', rToe: 'RightToeBase',
};
// 위에서 아래로 계산하는 순서
const ORDER = ['hips','spine','spine1','spine2','neck','head','lClav','lArm','lFore','lHand','rClav','rArm','rFore','rHand','lUp','lLeg','lFoot','lToe','rUp','rLeg','rFoot','rToe'];

function initRig() {
  character.updateMatrixWorld(true);
  for (const [k, n] of Object.entries(BONES)) {
    const b = model.getObjectByName('mixamorig' + n);
    if (!b) throw new Error('bone ' + n);
    B[k] = b;
    rest[k] = {local: b.quaternion.clone(), world: b.getWorldQuaternion(new THREE.Quaternion()), pos: b.getWorldPosition(new THREE.Vector3())};
  }
  rest.charQ = character.getWorldQuaternion(new THREE.Quaternion());
  rest.charInv = character.matrixWorld.clone().invert();
  const wp = (k) => rest[k].pos;
  // 팔다리 기준 자세의 방향과 접히는 축(경첩)
  const limb = (a, b, hinge) => ({dir: wp(b).clone().sub(wp(a)).normalize(), hinge});
  rest.lArmL = limb('lArm', 'lFore', new THREE.Vector3(0, -1, 0));
  rest.lForeL = limb('lFore', 'lHand', new THREE.Vector3(0, -1, 0));
  rest.rArmL = limb('rArm', 'rFore', new THREE.Vector3(0, 1, 0));
  rest.rForeL = limb('rFore', 'rHand', new THREE.Vector3(0, 1, 0));
  rest.lUpL = limb('lUp', 'lLeg', new THREE.Vector3(1, 0, 0));
  rest.lLegL = limb('lLeg', 'lFoot', new THREE.Vector3(1, 0, 0));
  rest.rUpL = limb('rUp', 'rLeg', new THREE.Vector3(1, 0, 0));
  rest.rLegL = limb('rLeg', 'rFoot', new THREE.Vector3(1, 0, 0));
  rest.lHandB = basis(new THREE.Vector3(1, 0, 0), new THREE.Vector3(0, -1, 0));
  rest.rHandB = basis(new THREE.Vector3(-1, 0, 0), new THREE.Vector3(0, -1, 0));
  for (const k of ['lArmL','lForeL','rArmL','rForeL','lUpL','lLegL','rUpL','rLegL']) {
    rest[k].B = basis(rest[k].dir, rest[k].hinge);
  }
}

// 몸 기준 회전 Q에서, 기준 자세의 팔다리 방향을 지금 방향으로 가장 짧게 돌렸을 때의 경첩축
function carriedHinge(bodyQ, restDir, restHinge, dir) {
  const inv = bodyQ.clone().invert();
  const dLocal = dir.clone().applyQuaternion(inv);
  const q = new THREE.Quaternion().setFromUnitVectors(restDir, dLocal);
  return restHinge.clone().applyQuaternion(q).applyQuaternion(bodyQ);
}
function smooth(e0, e1, x) { const t = Math.min(1, Math.max(0, (x - e0) / (e1 - e0))); return t * t * (3 - 2 * t); }

// 두 마디(위팔-아래팔, 허벅지-정강이)의 경첩축
function hingeFor(bodyQ, rl, upper, lower) {
  const fb = carriedHinge(bodyQ, rl.dir, rl.hinge, upper);
  const cr = new THREE.Vector3().crossVectors(upper, lower);
  const s = cr.length();
  const w = smooth(0.06, 0.3, s);
  if (w <= 0) return fb;
  cr.normalize();
  // fb를 upper 축으로 돌려 cr쪽으로 w만큼
  const u = upper.clone().normalize();
  const f = fb.clone().addScaledVector(u, -fb.dot(u)).normalize();
  const c2 = cr.clone().addScaledVector(u, -cr.dot(u)).normalize();
  const ang = Math.atan2(new THREE.Vector3().crossVectors(f, c2).dot(u), f.dot(c2));
  return f.applyAxisAngle(u, ang * w);
}

const W = {};
function setWorld(k, q) {
  W[k] = q;
  const parentW = parentWorld(k);
  B[k].quaternion.copy(parentW.clone().invert().multiply(q));
}
function carry(k) { // 부모를 따라가기만
  const q = parentWorld(k).clone().multiply(rest[k].local);
  W[k] = q; B[k].quaternion.copy(rest[k].local);
}
const PARENT = {hips: null, spine: 'hips', spine1: 'spine', spine2: 'spine1', neck: 'spine2', head: 'neck',
  lClav: 'spine2', lArm: 'lClav', lFore: 'lArm', lHand: 'lFore', rClav: 'spine2', rArm: 'rClav', rFore: 'rArm', rHand: 'rFore',
  lUp: 'hips', lLeg: 'lUp', lFoot: 'lLeg', lToe: 'lFoot', rUp: 'hips', rLeg: 'rUp', rFoot: 'rLeg', rToe: 'rFoot'};
function parentWorld(k) { return PARENT[k] ? W[PARENT[k]] : rest.charQ; }

const L = (lerped, n) => lerped[n];
function applyPose(P) {
  const p = (n) => P[J[n]];
  const sub = (a, b) => p(a).clone().sub(p(b));
  const hipMid = p('L_HIP').clone().add(p('R_HIP')).multiplyScalar(0.5);
  const hipLine = sub('L_HIP', 'R_HIP');
  const shLine = sub('L_SHOULDER', 'R_SHOULDER');
  const qHips = bodyBasis(p('PELVIS').clone().sub(hipMid), hipLine);
  const qLow = bodyBasis(sub('CHEST', 'PELVIS'), hipLine);
  const qUp = bodyBasis(sub('NECK', 'CHEST'), shLine);
  const qMid = qLow.clone().slerp(qUp, 0.5);
  const qHead = faceBasis(sub('FACE', 'HEAD'), shLine);
  const qNeck = bodyBasis(sub('HEAD', 'NECK'), shLine).slerp(qHead, 0.3);

  // 엉덩이 위치
  const hp = p('PELVIS');
  B.hips.position.copy(hp).applyMatrix4(rest.charInv);
  setWorld('hips', qHips.clone().multiply(rest.hips.world));
  setWorld('spine', qLow.clone().multiply(rest.spine.world));
  setWorld('spine1', qMid.clone().multiply(rest.spine1.world));
  setWorld('spine2', qUp.clone().multiply(rest.spine2.world));
  setWorld('neck', qNeck.clone().multiply(rest.neck.world));
  setWorld('head', qHead.clone().multiply(rest.head.world));

  for (const s of ['l', 'r']) {
    const S = s.toUpperCase();
    const up = sub(S + '_ELBOW', S + '_SHOULDER').normalize();
    const lo = sub(S + '_WRIST', S + '_ELBOW').normalize();
    // 쇄골: 팔을 머리 위로 들면 어깨도 살짝 올라가요
    const bodyUp = new THREE.Vector3(0, 1, 0).applyQuaternion(qUp);
    const raise = smooth(0.2, 0.95, up.dot(bodyUp));
    const fwd = new THREE.Vector3(0, 0, 1).applyQuaternion(qUp);
    const clav = new THREE.Quaternion().setFromAxisAngle(fwd, (s === 'l' ? 1 : -1) * raise * 0.28);
    setWorld(s + 'Clav', clav.multiply(qUp).multiply(rest[s + 'Clav'].world));
    const rl = rest[s + 'ArmL'], rf = rest[s + 'ForeL'];
    const h = hingeFor(qUp, rl, up, lo);
    const qa = basis(up, h).multiply(rl.B.clone().invert());
    setWorld(s + 'Arm', qa.multiply(rest[s + 'Arm'].world));
    const qf = basis(lo, h).multiply(rf.B.clone().invert());
    setWorld(s + 'Fore', qf.multiply(rest[s + 'Fore'].world));
    carry(s + 'Hand');
    // 손목이 바닥 가까이 있으면 손바닥을 바닥에 평평하게 짚어요(손가락은 머리 쪽)
    const wy = p(S + '_WRIST').y;
    const flat = 1 - smooth(0.07, 0.2, wy);
    if (flat > 0) {
      const head = new THREE.Vector3(0, 1, 0).applyQuaternion(qUp).setY(0);
      if (head.lengthSq() > 1e-4) {
        head.normalize();
        const rh = rest[s + 'HandB'];
        const target = basis(head, new THREE.Vector3(0, -1, 0)).multiply(rh.clone().invert()).multiply(rest[s + 'Hand'].world);
        setWorld(s + 'Hand', W[s + 'Hand'].clone().slerp(target, flat));
      }
    }

    const th = sub(S + '_KNEE', S + '_HIP').normalize();
    const sh = sub(S + '_ANKLE', S + '_KNEE').normalize();
    const ru = rest[s + 'UpL'], rg = rest[s + 'LegL'];
    const lh = hingeFor(qHips, ru, th, sh);
    setWorld(s + 'Up', basis(th, lh).multiply(ru.B.clone().invert()).multiply(rest[s + 'Up'].world));
    setWorld(s + 'Leg', basis(sh, lh).multiply(rg.B.clone().invert()).multiply(rest[s + 'Leg'].world));
    // 발: 앞(발끝)과 정강이 위쪽으로 방향을 정해요
    const toe = sub(S + '_TOE', S + '_ANKLE');
    setWorld(s + 'Foot', faceBasis(toe, lh).multiply(rest[s + 'Foot'].world));
    carry(s + 'Toe');
  }
}

// ── 동작 데이터 ──────────────────────────────────────
let data = null; // {period, frames:[[Vector3...]], ground:[], box}
let time = 0, playing = true, speed = 1;
const sampleVerts = [];

// 바닥 맞춤에 쓸 정점(머리카락·머리는 빼요: 누우면 머리카락은 눌리니까)
let groundIdx = null;
function buildGroundIdx() {
  groundIdx = skinned.map(mesh => {
    const si = mesh.geometry.attributes.skinIndex, sw = mesh.geometry.attributes.skinWeight, bones = mesh.skeleton.bones;
    const out = [];
    for (let i = 0; i < si.count; i += 2) {
      let best = 0, bw = -1;
      for (let k = 0; k < 4; k++) { const w = sw.getComponent(i, k); if (w > bw) { bw = w; best = si.getComponent(i, k); } }
      if (!/Head/.test(bones[best].name) || /Hand/.test(bones[best].name)) out.push(i);
    }
    return out;
  });
}
function lowestY() {
  model.updateMatrixWorld(true);
  let m = Infinity;
  const v = new THREE.Vector3();
  skinned.forEach((mesh, k) => {
    for (const i of groundIdx[k]) {
      mesh.getVertexPosition(i, v); v.applyMatrix4(mesh.matrixWorld);
      if (v.y < m) m = v.y;
    }
  });
  return m;
}
function meshBox(box) {
  model.updateMatrixWorld(true);
  const v = new THREE.Vector3();
  for (const mesh of skinned) {
    const n = mesh.geometry.attributes.position.count;
    for (let i = 0; i < n; i += 9) { mesh.getVertexPosition(i, v); v.applyMatrix4(mesh.matrixWorld); box.expandByPoint(v); }
  }
}

let focusUpper = false;
// 노란 정렬선(다림줄): 서서 하는 운동에서 발목 사이를 지나는 세로선
const plumb = new THREE.Group();
{
  const mat = new THREE.MeshBasicMaterial({color: 0xffc93c, transparent: true, opacity: 0.9, depthTest: false});
  const line = new THREE.Mesh(new THREE.CylinderGeometry(0.0045, 0.0045, 1.9, 8), mat);
  line.position.y = 0.95;
  const bob = new THREE.Mesh(new THREE.ConeGeometry(0.018, 0.045, 16), mat);
  bob.rotation.x = Math.PI; bob.position.y = 0.03;
  plumb.add(line, bob);
  plumb.renderOrder = 10; line.renderOrder = 10; bob.renderOrder = 10;
  plumb.visible = false;
  scene.add(plumb);
}
function load(json, opts) {
  const d = typeof json === 'string' ? JSON.parse(json) : json;
  opts = opts || {};
  focusUpper = !!opts.focusUpper;
  const nJ = d.joints.length;
  const frames = d.frames.map(f => { const a = []; for (let i = 0; i < nJ; i++) a.push(new THREE.Vector3(f[3 * i], f[3 * i + 1], f[3 * i + 2])); return a; });
  const ground = [];
  const box = new THREE.Box3();
  const focusBox = new THREE.Box3();
  for (let i = 0; i < frames.length; i++) {
    applyPose(frames[i]);
    const dy = -lowestY() + (d.lift ? d.lift[i] : 0);
    ground.push(dy);
    if (i % 3 === 0) {
      model.position.y = dy;
      meshBox(box);
      if (focusUpper) {
        const fb = new THREE.Box3(); meshBox(fb);
        fb.min.y = Math.max(fb.min.y, frames[i][J.PELVIS].y + dy - 0.12);
        focusBox.union(fb);
      }
      model.position.y = 0;
    }
  }
  data = {period: d.period, frames, ground, box: focusUpper ? focusBox : box};
  const f0 = frames[0];
  const standing = f0[J.HEAD].y - f0[J.L_ANKLE].y > 1.1;
  plumb.visible = !!opts.plumb && standing && !focusUpper;
  plumb.position.set((f0[J.L_ANKLE].x + f0[J.R_ANKLE].x) / 2, 0, (f0[J.L_ANKLE].z + f0[J.R_ANKLE].z) / 2);
  if (opts.time != null) time = opts.time;
  fit();
  render();
}

// ── 카메라 ───────────────────────────────────────────
let yaw = 38, elev = 14, yawTarget = 38, elevTarget = 14, dist = 4, center = new THREE.Vector3(0, 0.8, 0);
function camDir(y, e) {
  const r = Math.PI / 180;
  return new THREE.Vector3(-Math.sin(y * r) * Math.cos(e * r), Math.sin(e * r), Math.cos(y * r) * Math.cos(e * r));
}
function fit() {
  if (!data) return;
  const b = data.box;
  center = b.getCenter(new THREE.Vector3());
  if (!focusUpper) center.y = Math.max(center.y, (b.min.y + b.max.y) / 2);
  const corners = [];
  for (const x of [b.min.x, b.max.x]) for (const y of [b.min.y, b.max.y]) for (const z of [b.min.z, b.max.z]) corners.push(new THREE.Vector3(x, y, z).sub(center));
  const tv = Math.tan(camera.fov * Math.PI / 360) * 0.9;
  const th = tv * camera.aspect;
  let need = 0.5;
  for (const yy of [0, 45, 90, 135, yawTarget]) {
    const fwd = camDir(yy, elevTarget).negate();
    const right = new THREE.Vector3().crossVectors(fwd, new THREE.Vector3(0, 1, 0)).normalize();
    const upv = new THREE.Vector3().crossVectors(right, fwd);
    for (const c of corners) {
      const z = -c.dot(fwd); // 카메라 쪽으로 나온 거리
      need = Math.max(need, Math.abs(c.dot(upv)) / tv + z, Math.abs(c.dot(right)) / th + z);
    }
  }
  dist = need;
}
function placeCamera() {
  camera.position.copy(center).addScaledVector(camDir(yaw, elev), dist);
  camera.lookAt(center);
}

// ── 그리기 ───────────────────────────────────────────
function poseAtTime(t) {
  const n = data.frames.length;
  const ph = ((t / data.period) % 1 + 1) % 1 * n;
  const i = Math.floor(ph) % n, k = (i + 1) % n, f = ph - Math.floor(ph);
  const A = data.frames[i], Bf = data.frames[k];
  const P = A.map((a, j) => a.clone().lerp(Bf[j], f));
  return {P, dy: data.ground[i] * (1 - f) + data.ground[k] * f};
}
function render() {
  if (!resize()) return;
  if (!model || !data) { renderer.render(scene, camera); return; }
  const {P, dy} = poseAtTime(time);
  applyPose(P);
  model.position.y = dy;
  placeCamera();
  renderer.render(scene, camera);
}

// 화면 크기: WebView가 처음엔 크기 0으로 열릴 수 있어서, 크기가 생기거나 바뀔 때마다 다시 맞춰요.
let sizeW = 0, sizeH = 0;
function resize() {
  // 화면(창) 크기를 기준으로 해요. 캔버스 높이가 0으로 잡히는 WebView가 있어서 크기를 직접 정해 줘요.
  const w = window.innerWidth || canvas.clientWidth, h = window.innerHeight || canvas.clientHeight;
  if (w < 2 || h < 2) return false;
  if (w === sizeW && h === sizeH && canvas.clientHeight > 1) return true;
  sizeW = w; sizeH = h;
  renderer.setSize(w, h, true);
  camera.aspect = w / h; camera.updateProjectionMatrix();
  fit();
  return true;
}
window.addEventListener('resize', () => { if (resize()) render(); });
if (window.ResizeObserver) new ResizeObserver(() => { if (resize()) render(); }).observe(canvas);

let last = performance.now();
function loop(now) {
  const dt = Math.min(0.1, (now - last) / 1000); last = now;
  if (playing) time += dt * speed;
  const k = 1 - Math.exp(-dt * 8);
  yaw += (yawTarget - yaw) * k; elev += (elevTarget - elev) * k;
  render();
  requestAnimationFrame(loop);
}

// 손가락으로 돌려 보기
let drag = null;
canvas.addEventListener('pointerdown', e => { drag = {x: e.clientX, y: e.clientY, yaw: yawTarget, elev: elevTarget}; canvas.setPointerCapture(e.pointerId); });
canvas.addEventListener('pointermove', e => {
  if (!drag) return;
  yawTarget = drag.yaw - (e.clientX - drag.x) * 0.5;
  yaw = yawTarget;
});
const endDrag = () => { if (drag && window.Android && Android.onYaw) Android.onYaw(yawTarget); drag = null; };
canvas.addEventListener('pointerup', endDrag); canvas.addEventListener('pointercancel', endDrag);

function contacts(t) {
  const {P, dy} = poseAtTime(t); applyPose(P); model.position.y = dy; model.updateMatrixWorld(true);
  const groups = {}; const v = new THREE.Vector3();
  for (const mesh of skinned) {
    const si = mesh.geometry.attributes.skinIndex, sw = mesh.geometry.attributes.skinWeight, bones = mesh.skeleton.bones;
    for (let i = 0; i < si.count; i += 2) {
      let best = 0, bw = -1; for (let k = 0; k < 4; k++) { const w = sw.getComponent(i, k); if (w > bw) { bw = w; best = si.getComponent(i, k); } }
      const n = bones[best].name.replace('mixamorig', '').replace(/(Left|Right)Hand\w+/, '$1Hand');
      mesh.getVertexPosition(i, v); v.applyMatrix4(mesh.matrixWorld);
      if (!(n in groups) || v.y < groups[n]) groups[n] = +v.y.toFixed(3);
    }
  }
  return groups;
}
window.SF = {contacts,dbg: () => ({box: data && data.box, center, dist, cw: canvas.clientWidth, ch: canvas.clientHeight, f0: data && data.frames[0][3]}),
  load,
  setView(y, e, instant) { yawTarget = y; elevTarget = e; if (instant) { yaw = y; elev = e; } fit(); },
  /** 배경을 불투명한 색으로 칠해요(투명 WebView에서 3D가 안 보이는 기기가 있어요). */
  setBg(hex) {
    renderer.setClearColor(new THREE.Color(hex), 1);
    document.documentElement.style.background = document.body.style.background = hex;
    render();
  },
  setTime(t) { time = t; },
  setPlaying(p) { playing = !!p; },
  setSpeed(s) { speed = s; },
  /** 진단: 지금 크기와 실제로 그려진 픽셀 비율을 돌려줘요. */
  probe() {
    const ok = resize();
    const gl = renderer.getContext();
    if (!ok || !gl) return JSON.stringify({w: canvas.clientWidth, h: canvas.clientHeight, gl: !!gl, drawn: 0});
    render();
    const W = gl.drawingBufferWidth, H = gl.drawingBufferHeight, px = new Uint8Array(W * H * 4);
    gl.readPixels(0, 0, W, H, gl.RGBA, gl.UNSIGNED_BYTE, px);
    const c = new THREE.Color(); renderer.getClearColor(c); const a0 = renderer.getClearAlpha();
    const hx = c.getHex(), cr = hx >> 16 & 255, cg = hx >> 8 & 255, cb = hx & 255, ca = a0 * 255;
    let n = 0, hit = 0;
    for (let i = 0; i < px.length; i += 4 * 97) {
      n++;
      if (Math.abs(px[i + 3] - ca) > 6 || (a0 > 0 && (Math.abs(px[i] - cr) + Math.abs(px[i + 1] - cg) + Math.abs(px[i + 2] - cb) > 18))) hit++;
    }
    return JSON.stringify({w: canvas.clientWidth, h: canvas.clientHeight, bw: W, bh: H, gl: true, lost: gl.isContextLost(), drawn: n ? hit / n : 0, cam: [camera.aspect, dist]});
  },
  renderAt(t) { time = t; yaw = yawTarget; elev = elevTarget; render(); },
};

new GLTFLoader().load(window.SF_MODEL || 'Michelle.glb', g => {
  model = g.scene;
  character = model.getObjectByName('Character') || model.children[0];
  model.traverse(o => {
    if (o.isMesh) {
      o.castShadow = true; o.receiveShadow = true; o.frustumCulled = false;
      if (o.isSkinnedMesh) skinned.push(o);
      if (o.material) { o.material.envMapIntensity = 1; }
    }
  });
  scene.add(model);
  model.traverse(o => { if (o.isMesh && o.material) {
    const m = o.material; m.roughness = Math.max(m.roughness ?? 0.6, 0.62); m.metalness = 0;
    if ('clearcoat' in m) m.clearcoat = 0; if ('sheen' in m) m.sheen = 0; if ('specularIntensity' in m) m.specularIntensity = 0.5;
    m.needsUpdate = true; } });
  initRig();
  buildGroundIdx();
  resize();
  window.SF_READY = true;
  if (window.Android && Android.onReady) Android.onReady();
  if (!window.SF_SHOT) requestAnimationFrame(loop);
}, undefined, e => { window.SF_ERROR = String(e); if (window.Android && Android.onError) Android.onError(String(e)); });

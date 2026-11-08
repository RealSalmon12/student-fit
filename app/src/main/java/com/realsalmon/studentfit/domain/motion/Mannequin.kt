package com.realsalmon.studentfit.domain.motion

import kotlin.math.sqrt

/** 마네킹 부위의 재질(색은 화면에서 정해요). */
enum class Material { SKIN, SHIRT, SHORTS, SHOE, HAIR, FACE, EYE }

/** 3D 도형. 캡슐은 두 점 사이를 반지름이 변하는 원기둥으로 이어요. */
sealed interface Solid {
    val material: Material
    /** 같은 깊이일 때 나중에 그릴수록 큰 값(옷이 피부 위에 오도록). */
    val layer: Int
    /** 오른쪽 팔다리는 살짝 어둡게 그려 왼쪽과 구별해요. */
    val rightSide: Boolean
}

data class Capsule(
    val a: Vec3, val b: Vec3, val ra: Float, val rb: Float,
    override val material: Material, override val layer: Int = 0, override val rightSide: Boolean = false,
) : Solid

data class Sphere(
    val c: Vec3, val r: Float,
    override val material: Material, override val layer: Int = 0, override val rightSide: Boolean = false,
    /** 이 점(머리 중심)보다 카메라에서 멀면 그리지 않아요. 눈이 뒤통수로 비치지 않게. */
    val hideBehind: Vec3? = null,
) : Solid

/**
 * 관절 위치로 옷 입은 입체 마네킹을 만들어요.
 * 반지름은 키 1.75 기준 학생 체형에 맞췄어요.
 */
object Mannequin {

    private fun Vec3.len() = sqrt(x * x + y * y + z * z)
    private fun Vec3.norm(): Vec3 { val l = len().coerceAtLeast(1e-5f); return Vec3(x / l, y / l, z / l) }
    private fun cross(a: Vec3, b: Vec3) = Vec3(a.y * b.z - a.z * b.y, a.z * b.x - a.x * b.z, a.x * b.y - a.y * b.x)
    private fun mix(a: Vec3, b: Vec3, t: Float) = a + (b - a) * t

    fun build(f: SkeletonFrame): List<Solid> {
        val out = ArrayList<Solid>(40)
        fun cap(a: Joint, b: Joint, ra: Float, rb: Float, mat: Material, layer: Int = 0, rightSide: Boolean = false) {
            out += Capsule(f[a], f[b], ra, rb, mat, layer, rightSide)
        }

        // 몸통: 골반(반바지) → 배·가슴(티셔츠) → 목. 옆구리 캡슐로 어깨가 넓은 사다리꼴 몸통을 만들어요.
        cap(Joint.L_HIP, Joint.R_HIP, 0.112f, 0.112f, Material.SHORTS)
        out += Capsule(f[Joint.PELVIS], mix(f[Joint.PELVIS], f[Joint.CHEST], 0.35f), 0.128f, 0.126f, Material.SHORTS)
        out += Capsule(mix(f[Joint.PELVIS], f[Joint.CHEST], 0.4f), f[Joint.CHEST], 0.118f, 0.13f, Material.SHIRT, layer = 1)
        out += Capsule(f[Joint.CHEST], mix(f[Joint.CHEST], f[Joint.NECK], 0.72f), 0.135f, 0.128f, Material.SHIRT, layer = 1)
        for ((sh, hp, right) in listOf(Triple(Joint.L_SHOULDER, Joint.L_HIP, false), Triple(Joint.R_SHOULDER, Joint.R_HIP, true))) {
            val top = mix(f[sh], f[Joint.NECK], 0.2f) + (f[Joint.CHEST] - f[Joint.NECK]) * 0.25f
            val waist = mix(f[hp], f[sh], 0.36f)
            out += Capsule(top, waist, 0.085f, 0.088f, Material.SHIRT, layer = 1, rightSide = right)
            out += Capsule(waist, f[hp], 0.09f, 0.1f, Material.SHORTS, rightSide = right)
        }
        cap(Joint.L_SHOULDER, Joint.R_SHOULDER, 0.07f, 0.07f, Material.SHIRT, layer = 2)
        out += Capsule(f[Joint.NECK], mix(f[Joint.NECK], f[Joint.HEAD], 0.55f), 0.05f, 0.046f, Material.SKIN)

        // 팔: 위팔(반팔 소매) → 아래팔 → 손
        for ((side, far) in listOf(0 to false, 1 to true)) {
            val sh = if (side == 0) Joint.L_SHOULDER else Joint.R_SHOULDER
            val el = if (side == 0) Joint.L_ELBOW else Joint.R_ELBOW
            val wr = if (side == 0) Joint.L_WRIST else Joint.R_WRIST
            val hd = if (side == 0) Joint.L_HAND else Joint.R_HAND
            cap(sh, el, 0.056f, 0.046f, Material.SKIN, rightSide = far)
            out += Capsule(f[sh], mix(f[sh], f[el], 0.5f), 0.066f, 0.058f, Material.SHIRT, layer = 2, rightSide = far)
            cap(el, wr, 0.045f, 0.034f, Material.SKIN, rightSide = far)
            out += Capsule(f[wr], f[hd], 0.037f, 0.03f, Material.SKIN, rightSide = far)
        }

        // 다리: 허벅지(반바지) → 정강이 → 신발
        for ((side, far) in listOf(0 to false, 1 to true)) {
            val hp = if (side == 0) Joint.L_HIP else Joint.R_HIP
            val kn = if (side == 0) Joint.L_KNEE else Joint.R_KNEE
            val an = if (side == 0) Joint.L_ANKLE else Joint.R_ANKLE
            val toe = if (side == 0) Joint.L_TOE else Joint.R_TOE
            cap(hp, kn, 0.088f, 0.06f, Material.SKIN, rightSide = far)
            out += Capsule(f[hp], mix(f[hp], f[kn], 0.5f), 0.1f, 0.082f, Material.SHORTS, layer = 1, rightSide = far)
            cap(kn, an, 0.058f, 0.04f, Material.SKIN, rightSide = far)
            // 신발: 발목 조금 위에서 발끝 조금 앞까지
            val a = f[an]; val t = f[toe]
            out += Capsule(mix(a, t, -0.25f), mix(a, t, 1.15f), 0.042f, 0.036f, Material.SHOE, layer = 1, rightSide = far)
        }

        // 머리: 뒤통수 쪽 머리카락 + 얼굴 쪽 피부 + 눈 두 개
        val head = f[Joint.HEAD]
        val face = (f[Joint.FACE] - head).norm()
        val up = (head - f[Joint.NECK]).norm()
        val side = cross(up, face).norm()
        val r = Skeleton.HEAD_RADIUS
        out += Sphere(head - face * (r * 0.14f) + up * (r * 0.1f), r * 1.03f, Material.HAIR)
        out += Sphere(head + face * (r * 0.14f) - up * (r * 0.08f), r * 0.88f, Material.SKIN, layer = 1)
        for (sgn in listOf(-1f, 1f)) {
            val eye = head + face * (r * 0.98f) + side * (r * 0.34f * sgn) + up * (r * 0.02f)
            out += Sphere(eye, r * 0.085f, Material.EYE, layer = 3, hideBehind = head)
        }
        out += Sphere(head + face * (r * 1.0f) - up * (r * 0.3f), r * 0.1f, Material.FACE, layer = 2, hideBehind = head)
        return out
    }
}

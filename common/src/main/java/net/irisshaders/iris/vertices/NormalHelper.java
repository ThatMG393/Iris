package net.irisshaders.iris.vertices;

import net.irisshaders.iris.vertices.views.QuadView;
import net.irisshaders.iris.vertices.views.TriView;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public final class NormalHelper {
    private static final int FLOAT_SIGN_MASK = 0x80000000;
    private static final int FLOAT_ABS_MASK = 0x7FFFFFFF;
    private static final int FLOAT_EXP_MASK = 0x7F800000;
    private static final int BYTE_MASK = 0xFF;
    private static final float EPSILON = Float.intBitsToFloat(0x33FFFFFF);
    private static final float ONE_OVER_ROOT_TWO = 0.70710678118f;

    private NormalHelper() {}

    public static int invertPackedNormal(int packed) {
        int x = -(packed & BYTE_MASK);
        int y = -((packed >>> 8) & BYTE_MASK);
        int z = -((packed >>> 16) & BYTE_MASK);
        return (packed & 0xFF000000) | 
               (x & BYTE_MASK) | 
               ((y & BYTE_MASK) << 8) | 
               ((z & BYTE_MASK) << 16);
    }

    private static float fastAbs(float f) {
        return Float.intBitsToFloat(Float.floatToRawIntBits(f) & FLOAT_ABS_MASK);
    }

    private static int fastSign(float f) {
        return (Float.floatToRawIntBits(f) >>> 31) == 0 ? 1 : -1;
    }

    private static float fastInvSqrt(float x) {
        int bits = Float.floatToRawIntBits(x);
        bits = 0x5f3759df - (bits >> 1);
        float y = Float.intBitsToFloat(bits);
        return y * (1.5f - 0.5f * x * y * y);
    }

    public static void computeFaceNormal(@NotNull Vector3f saveTo, QuadView q) {
        float x0 = q.x(0), y0 = q.y(0), z0 = q.z(0);
        float x1 = q.x(1), y1 = q.y(1), z1 = q.z(1);
        float x2 = q.x(2), y2 = q.y(2), z2 = q.z(2);
        float x3 = q.x(3), y3 = q.y(3), z3 = q.z(3);

        float nx = (y1 - y0) * (z2 - z0) - (y2 - y0) * (z1 - z0);
        float ny = (z1 - z0) * (x2 - x0) - (z2 - z0) * (x1 - x0);
        float nz = (x1 - x0) * (y2 - y0) - (x2 - x0) * (y1 - y0);

        float invLen = fastInvSqrt(nx * nx + ny * ny + nz * nz);

        saveTo.set(nx * invLen, ny * invLen, nz * invLen);
    }

    public static void computeFaceNormalFlipped(@NotNull Vector3f saveTo, QuadView q) {
        float x0 = q.x(3), y0 = q.y(3), z0 = q.z(3);
        float x1 = q.x(2), y1 = q.y(2), z1 = q.z(2);
        float x2 = q.x(1), y2 = q.y(1), z2 = q.z(1);
        float x3 = q.x(0), y3 = q.y(0), z3 = q.z(0);

        float nx = (y1 - y0) * (z2 - z0) - (y2 - y0) * (z1 - z0);
        float ny = (z1 - z0) * (x2 - x0) - (z2 - z0) * (x1 - x0);
        float nz = (x1 - x0) * (y2 - y0) - (x2 - x0) * (y1 - y0);

        float invLen = fastInvSqrt(nx * nx + ny * ny + nz * nz);

        saveTo.set(nx * invLen, ny * invLen, nz * invLen);
    }

    public static void computeFaceNormalTri(@NotNull Vector3f saveTo, TriView t) {
        float x0 = t.x(0), y0 = t.y(0), z0 = t.z(0);
        float x1 = t.x(1), y1 = t.y(1), z1 = t.z(1);
        float x2 = t.x(2), y2 = t.y(2), z2 = t.z(2);

        float nx = (y1 - y0) * (z2 - z0) - (y2 - y0) * (z1 - z0);
        float ny = (z1 - z0) * (x2 - x0) - (z2 - z0) * (x1 - x0);
        float nz = (x1 - x0) * (y2 - y0) - (x2 - x0) * (y1 - y0);

        float invLen = fastInvSqrt(nx * nx + ny * ny + nz * nz);

        saveTo.set(nx * invLen, ny * invLen, nz * invLen);
    }

    public static int computeTangent(float normalX, float normalY, float normalZ, TriView t) {
        float x0 = t.x(0), y0 = t.y(0), z0 = t.z(0), u0 = t.u(0), v0 = t.v(0);
        float x1 = t.x(1), y1 = t.y(1), z1 = t.z(1), u1 = t.u(1), v1 = t.v(1);
        float x2 = t.x(2), y2 = t.y(2), z2 = t.z(2), u2 = t.u(2), v2 = t.v(2);

        float edge1x = x1 - x0, edge1y = y1 - y0, edge1z = z1 - z0;
        float edge2x = x2 - x0, edge2y = y2 - y0, edge2z = z2 - z0;
        float deltaU1 = u1 - u0, deltaV1 = v1 - v0;
        float deltaU2 = u2 - u0, deltaV2 = v2 - v0;

        float det = deltaU1 * deltaV2 - deltaU2 * deltaV1;
        int detBits = Float.floatToRawIntBits(det);
        float invDet = (detBits & FLOAT_SIGN_MASK) == 0 && fastAbs(det) > EPSILON 
            ? 1.0f / det 
            : 1.0f;

        float tx = (deltaV2 * edge1x - deltaV1 * edge2x) * invDet;
        float ty = (deltaV2 * edge1y - deltaV1 * edge2y) * invDet;
        float tz = (deltaV2 * edge1z - deltaV1 * edge2z) * invDet;
        float dot = tx * normalX + ty * normalY + tz * normalZ;
        tx -= dot * normalX;
        ty -= dot * normalY;
        tz -= dot * normalZ;

        float invLen = fastInvSqrt(tx * tx + ty * ty + tz * tz);

        tx *= invLen;
        ty *= invLen;
        tz *= invLen;

        float bitx = normalY * tz - normalZ * ty;
        float bity = normalZ * tx - normalX * tz;
        float bitz = normalX * ty - normalY * tx;
        int tangentW = (bitx * tx + bity * ty + bitz * tz) < 0 ? -1 : 1;

        return NormI8.pack(tx, ty, tz, tangentW);
    }

    public static void octahedronEncode(Vector2f output, float x, float y, float z) {
        float absX = fastAbs(x), absY = fastAbs(y), absZ = fastAbs(z);
        float sum = absX + absY + absZ;

        int sumBits = Float.floatToRawIntBits(sum);
        if ((sumBits & FLOAT_ABS_MASK) < Float.floatToRawIntBits(EPSILON)) {
            output.set(0.5f, 0.5f);
            return;
        }

        float invSum = 1.0f / sum;
        float nx = x * invSum, ny = y * invSum, nz = z * invSum;

        int nzSign = Float.floatToRawIntBits(nz) & FLOAT_SIGN_MASK;
        float oX = nzSign == 0 ? nx : ONE_OVER_ROOT_TWO * fastSign(nx);
        float oY = nzSign == 0 ? ny : ONE_OVER_ROOT_TWO * fastSign(ny);

        output.set(oX * 0.5f + 0.5f, oY * 0.5f + 0.5f);
    }

    public static int computeTangent(float normalX, float normalY, float normalZ, float x0, float y0, float z0, float u0, float v0,
                                     float x1, float y1, float z1, float u1, float v1,
                                     float x2, float y2, float z2, float u2, float v2) {
        return computeTangent(null, normalX, normalY, normalZ, 
            x0, y0, z0, u0, v0, 
            x1, y1, z1, u1, v1, 
            x2, y2, z2, u2, v2);
    }

    public static Vector3f octahedronDecode(float inX, float inY) {
        Vector2f f = new Vector2f(inX * 2.0f - 1.0f, inY * 2.0f - 1.0f);
        Vector3f n = new Vector3f(f.x, f.y, 1.0f - Math.abs(f.x) - Math.abs(f.y));
        
        float t = Math.max(Math.min(-n.z, 1.0f), 0.0f);
        n.x += n.x >= 0 ? -t : t;
        n.y += n.y >= 0 ? -t : t;
        
        return n.normalize();
    }

    public static int computeTangentSmooth(float normalX, float normalY, float normalZ, TriView t) {
        float x0 = t.x(0), y0 = t.y(0), z0 = t.z(0);
        float x1 = t.x(1), y1 = t.y(1), z1 = t.z(1);
        float x2 = t.x(2), y2 = t.y(2), z2 = t.z(2);

        float edge1x = x1 - x0, edge1y = y1 - y0, edge1z = z1 - z0;
        float edge2x = x2 - x0, edge2y = y2 - y0, edge2z = z2 - z0;

        float u0 = t.u(0), v0 = t.v(0);
        float u1 = t.u(1), v1 = t.v(1);
        float u2 = t.u(2), v2 = t.v(2);

        float deltaU1 = u1 - u0, deltaV1 = v1 - v0;
        float deltaU2 = u2 - u0, deltaV2 = v2 - v0;

        float d0 = x0 * normalX + y0 * normalY + z0 * normalZ;
        float d1 = x1 * normalX + y1 * normalY + z1 * normalZ;
        float d2 = x2 * normalX + y2 * normalY + z2 * normalZ;

        x0 -= d0 * normalX;
        y0 -= d0 * normalY;
        z0 -= d0 * normalZ;

        x1 -= d1 * normalX;
        y1 -= d1 * normalY;
        z1 -= d1 * normalZ;

        x2 -= d2 * normalX;
        y2 -= d2 * normalY;
        z2 -= d2 * normalZ;

        float det = deltaU1 * deltaV2 - deltaU2 * deltaV1;
        float invDet = Math.abs(det) > EPSILON ? 1.0f / det : 1.0f;

        float tx = (deltaV2 * edge1x - deltaV1 * edge2x) * invDet;
        float ty = (deltaV2 * edge1y - deltaV1 * edge2y) * invDet;
        float tz = (deltaV2 * edge1z - deltaV1 * edge2z) * invDet;

        float tcoeff = fastInvSqrt(tx * tx + ty * ty + tz * tz);
        tx *= tcoeff;
        ty *= tcoeff;
        tz *= tcoeff;

        float bitx = (normalY * tz - normalZ * ty);
        float bity = (normalZ * tx - normalX * tz);
        float bitz = (normalX * ty - normalY * tx);

        float tangentW = (bitx * tx + bity * ty + bitz * tz) < 0 ? -1.0f : 1.0f;

        return NormI8.pack(tx, ty, tz, tangentW);
    }

    public static void computeFaceNormalManual(@NotNull Vector3f saveTo,
                                               float x0, float y0, float z0,
                                               float x1, float y1, float z1,
                                               float x2, float y2, float z2,
                                               float x3, float y3, float z3) {
        float dx0 = x2 - x0, dy0 = y2 - y0, dz0 = z2 - z0;
        float dx1 = x3 - x1, dy1 = y3 - y1, dz1 = z3 - z1;

        float normX = dy0 * dz1 - dz0 * dy1;
        float normY = dz0 * dx1 - dx0 * dz1;
        float normZ = dx0 * dy1 - dy0 * dx1;

        float invLen = fastInvSqrt(normX * normX + normY * normY + normZ * normZ);
        saveTo.set(normX * invLen, normY * invLen, normZ * invLen);
    }
}

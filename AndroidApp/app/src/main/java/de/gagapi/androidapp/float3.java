package de.gagapi.androidapp;

import android.support.annotation.NonNull;

class float3 implements Comparable<float3>
{
    public float3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float3 add(float3 rhs)
    {
        this.x += rhs.x;
        this.y += rhs.y;
        this.z += rhs.z;
        return this;
    }

    public float3 sub(float3 rhs)
    {
        this.x -= rhs.x;
        this.y -= rhs.y;
        this.z -= rhs.z;
        return this;
    }

    public float3 mul(float3 rhs)
    {
        this.x *= rhs.x;
        this.y *= rhs.y;
        this.z *= rhs.z;
        return this;
    }


    public float3 mul(float rhs)
    {
        this.x *= rhs;
        this.y *= rhs;
        this.z *= rhs;
        return this;
    }

    public float length()
    {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    @Override
    public String toString() {
        return String.format("x: %+.5f, y: %+.5f, z: %+.5f", x, y, z);
    }

    public float x, y, z;

    @Override
    public int compareTo(@NonNull float3 o) {
        float thisLength = this.length();
        float otherLength = o.length();

        if (thisLength < otherLength) return -1;
        if (thisLength == otherLength) return 0;
       /* if (thisLength > otherLength)*/ return 1;
    }
}

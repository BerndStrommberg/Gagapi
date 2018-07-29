package de.gagapi.androidapp;

class LowPassFilter extends Filter
{
    /* Digital filter designed by mkfilter/mkshape/gencode   A.J. Fisher
    Command line: /www/usr/fisher/helpers/mkfilter -Bu -Lp -o 3 -a 4.0000000000e-01 0.0000000000e+00 -l */

    static final int NZEROS = 3;
    static final int NPOLES = 3;
    static final float GAIN  = 1.895287695e+00f;

    float[] xv = new float[NZEROS+1], yv = new float[NPOLES+1];

    @Override
    public float Step(float nextInputValue)
    {
        { xv[0] = xv[1]; xv[1] = xv[2]; xv[2] = xv[3];
            xv[3] = nextInputValue / GAIN;
            yv[0] = yv[1]; yv[1] = yv[2]; yv[2] = yv[3];
            yv[3] =  (xv[0] + xv[3]) + 3 * (xv[1] + xv[2])
                    + ( -0.2780599176f * yv[0]) + ( -1.1828932620f * yv[1])
                    + ( -1.7600418803f * yv[2]);
            return yv[3];
        }
    }
}

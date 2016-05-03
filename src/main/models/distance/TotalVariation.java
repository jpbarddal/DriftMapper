package main.models.distance;

/**
 * Created by loongkuan on 26/03/16.
 **/
public class TotalVariation extends Distance{
    @Override
    public double findDistance(double[] p, double[] q) {
        assert p.length == q.length;
        double driftMag = 0.0f;
        for (int i = 0; i < p.length; i++) {
            driftMag += Math.abs(p[i] - q[i]);
        }
        driftMag /= (double)2;
        return driftMag;
    }
}

package x.flyspace.shortpath;

import static java.lang.Double.MAX_VALUE;

/**
 * @author sky91 - feitiandaxia1991@163.com
 */
public class FloydMap {
    public final double[][] dist;

    public final int[][] path;

    public FloydMap(double[][] dist) {
        this.dist = new double[dist.length][dist.length];
        for(int i = 0; i < dist.length; i++) {
            System.arraycopy(dist[i], 0, this.dist[i], 0, dist.length);
        }
        path = new int[dist.length][dist.length];
        for(int i = 0; i < path.length; i++) {
            for(int j = 0; j < path.length; j++) {
                path[i][j] = i;
            }
        }
        calc();
    }

    private void calc() {
        for(int k = 0; k < dist.length; k++) {
            for(int i = 0; i < dist.length; i++) {
                for(int j = 0; j < dist.length; j++) {
                    double detour;
                    if(dist[i][k] != MAX_VALUE && dist[k][j] != MAX_VALUE && (detour = dist[i][k] + dist[k][j]) < dist[i][j]) {
                        dist[i][j] = detour;
                        path[i][j] = path[k][j];
                    }
                }
            }
        }
    }
}

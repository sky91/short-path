package x.flyspace.shortpath;

import x.flyspace.shortpath.InputData.City;
import x.flyspace.shortpath.InputData.Side;

import java.util.*;

/**
 * @author sky91 - feitiandaxia1991@163.com
 */
public class ShortPathMap {

    public final List<String> shortestPathList;

    public final double shortestWeight;

    public final List<Double> shortestPathWeight;

    private final List<String> cityNameList;

    private final Map<String, Integer> cityNameIndexMap;

    private final List<String> stationNameList;

    private final Map<String, Integer> stationNameIndexMap;

    private final ArrayList<NodeInfo> nodeInfoList;

    private final Map<NodeInfo, Integer> nodeInfoIndexMap;

    public ShortPathMap(InputData inputData) {
        Set<String> cityNameSet = new HashSet<>();
        inputData.cities.stream().map(city -> city.name).forEach(cityNameSet::add);
        inputData.sides.stream().flatMap(side -> side.cities.stream()).forEach(cityNameSet::add);
        cityNameList = new ArrayList<>(cityNameSet);
        cityNameIndexMap = reversMap(cityNameList);

        stationNameList = Arrays.asList("仓库", "汽车站", "火车站");
        stationNameIndexMap = reversMap(stationNameList);

        ArrayList<EdgeInfo> edgeInfoList = new ArrayList<>();
        for(City city : inputData.cities) {
            double transshipTime = city.transshipTime * inputData.load;
            double transshipCost = city.transshipPrice * inputData.load;
            double weight = weight(inputData.a, transshipTime, transshipCost);
            for(int i = 1; i < stationNameList.size(); i++) {
                for(int j = 1; j < stationNameList.size(); j++) {
                    if(i != j) {
                        edgeInfoList.add(new EdgeInfo(new NodeInfo(city.name, stationNameList.get(i)),
                                                      new NodeInfo(city.name, stationNameList.get(j)),
                                                      weight
                        ));
                    }
                }
            }
            if(city.name.equals(inputData.from)) {
                for(int i = 1; i < stationNameList.size(); i++) {
                    edgeInfoList.add(new EdgeInfo(new NodeInfo(city.name, "仓库"), new NodeInfo(city.name, stationNameList.get(i)), weight));
                }
            }
            else if(city.name.equals(inputData.to)) {
                for(int i = 1; i < stationNameList.size(); i++) {
                    edgeInfoList.add(new EdgeInfo(new NodeInfo(city.name, stationNameList.get(i)), new NodeInfo(city.name, "仓库"), weight));
                }
            }
        }

        for(Side side : inputData.sides) {
            double highwayTime = side.highwayDistance / inputData.highwaySpeed;
            double highwayCost = side.highwayDistance * inputData.highwayPrice * inputData.load;
            double highwayWeight = weight(inputData.a, highwayTime, highwayCost);

            double railwayTime = side.railwayDistance / inputData.railwaySpeed;
            double railwayCost = side.railwayDistance * inputData.railwayPrice * inputData.load;
            double railwayWeight = weight(inputData.a, railwayTime, railwayCost);

            List<String> cities = side.cities;
            for(int i = 0; i < cities.size(); i++) {
                for(int j = 0; j < cities.size(); j++) {
                    if(i != j) {
                        edgeInfoList.add(new EdgeInfo(new NodeInfo(cities.get(i), "汽车站"), new NodeInfo(cities.get(j), "汽车站"), highwayWeight));
                        edgeInfoList.add(new EdgeInfo(new NodeInfo(cities.get(i), "火车站"), new NodeInfo(cities.get(j), "火车站"), railwayWeight));
                    }
                }
            }
        }

        HashSet<NodeInfo> nodeInfoSet = new HashSet<>();
        for(EdgeInfo edgeInfo : edgeInfoList) {
            nodeInfoSet.add(edgeInfo.from);
            nodeInfoSet.add(edgeInfo.to);
        }
        nodeInfoList = new ArrayList<>(nodeInfoSet);
        nodeInfoIndexMap = reversMap(nodeInfoList);

        double[][] dist = new double[nodeInfoList.size()][nodeInfoList.size()];
        for(int i = 0; i < dist.length; i++) {
            for(int j = 0; j < dist.length; j++) {
                dist[i][j] = Double.MAX_VALUE;
            }
        }
        for(EdgeInfo edgeInfo : edgeInfoList) {
            dist[nodeInfoIndexMap.get(edgeInfo.from)][nodeInfoIndexMap.get(edgeInfo.to)] = edgeInfo.weight;
        }

        int from = nodeInfoIndexMap.get(new NodeInfo(inputData.from, "仓库"));
        int to = nodeInfoIndexMap.get(new NodeInfo(inputData.to, "仓库"));
        FloydMap floydMap = new FloydMap(dist);
        shortestWeight = floydMap.dist[from][to];

        ArrayList<Integer> pathNodeList = new ArrayList<>();
        int[][] path = floydMap.path;
        do {
            pathNodeList.add(to);
        } while((to = path[from][to]) != from);
        pathNodeList.add(to);

        shortestPathList = new ArrayList<>(pathNodeList.size());
        for(int i = pathNodeList.size() - 1; i >= 0; i--) {
            shortestPathList.add(nodeInfoList.get(pathNodeList.get(i)).nodeName());
        }

        shortestPathWeight = new ArrayList<>(pathNodeList.size() - 1);
        for(int i = pathNodeList.size() - 1; i >= 1; i--) {
            shortestPathWeight.add(dist[pathNodeList.get(i)][pathNodeList.get(i - 1)]);
        }
    }

    private double weight(double a, double time, double cost) {
        return a * time + (1 - a) / 10000 * cost;
    }

    private <T> Map<T, Integer> reversMap(List<T> list) {
        HashMap<T, Integer> map = new HashMap<>();
        for(int i = 0; i < list.size(); i++) {
            map.put(list.get(i), i);
        }
        return map;
    }

    public class NodeInfo {
        public final int city;

        public final int station;

        public NodeInfo(int city, int station) {
            this.city = city;
            this.station = station;
        }

        public NodeInfo(String cityName, String stationName) {
            this(cityNameIndexMap.get(cityName), stationNameIndexMap.get(stationName));
        }

        @Override
        public int hashCode() {
            return Objects.hash(city, station);
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) return true;
            if(o == null || getClass() != o.getClass()) return false;
            NodeInfo nodeInfo = (NodeInfo) o;
            return city == nodeInfo.city && station == nodeInfo.station;
        }

        @Override
        public String toString() {
            return nodeName();
        }

        public String nodeName() {
            return cityNameList.get(city) + stationNameList.get(station);
        }
    }

    public static class EdgeInfo {
        public final NodeInfo from;

        public final NodeInfo to;

        public final double weight;

        public EdgeInfo(NodeInfo from, NodeInfo to, double weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }

        @Override
        public String toString() {
            return from + " ---(" + weight + ")--> " + to;
        }
    }
}

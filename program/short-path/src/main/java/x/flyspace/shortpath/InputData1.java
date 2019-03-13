package x.flyspace.shortpath;

import x.flyspace.shortpath.InputData.City;
import x.flyspace.shortpath.InputData.Side;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * @author sky91 - feitiandaxia1991@163.com
 */
public class InputData1 {
    public String from;

    public String to;

    public double highwaySpeed;

    public double highwayPrice;

    public double railwaySpeed;

    public double railwayPrice;

    public double a;

    public double load;

    public List<String> cities;

    public List<String> sides;

    public InputData toInputData() {
        List<City> cityList = cities.stream().map(cityString -> {
            try(Scanner scanner = new Scanner(cityString)) {
                City city = new City();
                city.name = scanner.next();
                city.transshipTime = scanner.nextDouble();
                city.transshipPrice = scanner.nextDouble();
                return city;
            }
        }).collect(Collectors.toList());

        List<Side> sideList = sides.stream().map(sideString -> {
            try(Scanner scanner = new Scanner(sideString)) {
                Side side = new Side();
                side.cities = Arrays.asList(scanner.next(), scanner.next());
                side.highwayDistance = scanner.nextDouble();
                side.railwayDistance = scanner.nextDouble();
                return side;
            }
        }).collect(Collectors.toList());

        InputData inputData = new InputData();
        inputData.from = from;
        inputData.to = to;
        inputData.highwaySpeed = highwaySpeed;
        inputData.highwayPrice = highwayPrice;
        inputData.railwaySpeed = railwaySpeed;
        inputData.railwayPrice = railwayPrice;
        inputData.a = a;
        inputData.load = load;
        inputData.cities = cityList;
        inputData.sides = sideList;
        return inputData;
    }
}

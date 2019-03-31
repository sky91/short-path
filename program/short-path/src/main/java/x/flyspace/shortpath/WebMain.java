package x.flyspace.shortpath;

import com.alibaba.fastjson.JSON;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.MultipartAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author sky91 - feitiandaxia1991@163.com
 */
@SpringBootConfiguration
@Import({PropertyPlaceholderAutoConfiguration.class,
         ServletWebServerFactoryAutoConfiguration.class,
         DispatcherServletAutoConfiguration.class,
         WebMvcAutoConfiguration.class,
         MultipartAutoConfiguration.class})
@Controller
public class WebMain {
    @RequestMapping(path = "/short_path", produces = {"text/plain; charset=utf-8"})
    @ResponseBody
    public byte[] shortPath(@RequestParam("input_file") MultipartFile inputFile) throws IOException {
        InputData1 inputData1 = JSON.parseObject(new String(inputFile.getBytes(), UTF_8), InputData1.class);
        InputData inputData = inputData1.toInputData();
        ShortPathMap shortPathMap = new ShortPathMap(inputData);

        StringBuilder resultBuilder = new StringBuilder();
        List<Double> shortestPathWeight = shortPathMap.shortestPathWeight;
        List<String> shortestPathList = shortPathMap.shortestPathList;
        for(int i = 0; i < shortestPathWeight.size(); i++) {
            resultBuilder.append(shortestPathList.get(i)).append(" ---(").append(String.format("%.3f", shortestPathWeight.get(i))).append(")--> ");
        }
        resultBuilder
            .append(shortestPathList.get(shortestPathList.size() - 1))
            .append("\n")
            .append(String.format("总权重: %.3f", shortPathMap.shortestWeight));

        return resultBuilder.toString().getBytes(UTF_8);
    }

    public static void main(String[] args) throws InterruptedException {
        SpringApplication springApplication = new SpringApplicationBuilder(WebMain.class).properties("server.port=23233").build();
        springApplication.run(args).registerShutdownHook();
        TimeUnit.DAYS.sleep(Long.MAX_VALUE);
    }
}

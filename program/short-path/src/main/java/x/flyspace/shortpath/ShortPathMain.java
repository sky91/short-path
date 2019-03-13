package x.flyspace.shortpath;

import com.alibaba.fastjson.JSON;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.*;

/**
 * @author sky91 - feitiandaxia1991@163.com
 */
public class ShortPathMain {
    public static void main(String[] args) throws IOException {
        if(args.length < 1) {
            throw new IllegalArgumentException("需要输入数据路径作为首个参数");
        }
        Files.walk(Paths.get(args[0])).filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".json")).forEach(path -> {
            String pathString = path.toString();
            try {
                if(pathString.endsWith(".in1.json")) {
                    processInputData1Json(path);
                }
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void processInputData1Json(java.nio.file.Path path) throws IOException {
        String jsonString = new String(Files.readAllBytes(path), UTF_8);
        InputData inputData = JSON.parseObject(jsonString, InputData.class);
        ShortPathMap shortPathMap = new ShortPathMap(inputData);
        String
            result =
            String.join(" -> ", shortPathMap.shortestPathList) + "\n" + String.format("%.3f", shortPathMap.shortestWeight);

        String fileName = path.getFileName().toString();
        String fileNameWithoutExt = fileName.substring(0, fileName.length() - 9);
        Files.write(Paths.get(path.getParent().toString(), fileNameWithoutExt + ".result.txt"),
                    result.getBytes(UTF_8),
                    WRITE,
                    CREATE,
                    TRUNCATE_EXISTING
        );
    }
}

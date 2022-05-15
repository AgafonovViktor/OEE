import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class Main {

    public static List<Path> listPath;
    public static List<MachineLog> listMachine = new ArrayList<>();
    public static Logger logger;

    public static void main(String[] args) {

        logger = LogManager.getRootLogger();

        Scanner scanner = new Scanner(System.in);
        System.out.print("Введите путь к папке логами: ");
        String input = scanner.nextLine();
        logger.info("Путь к папке: " + input);

        System.out.print("Интервал времени для ПА (в формате 22.10.2021) от: ");
        String startTimeString = scanner.nextLine();
        if (!isValidDate(startTimeString)) {
            logger.fatal("Невалидная дата: " + startTimeString);
            throw new IllegalArgumentException("Невалидная дата");
        }
        Date startDate = parseDate(startTimeString);

        System.out.print("до: ");
        String finishTimeString = scanner.nextLine();
        if (!isValidDate(finishTimeString)) {
            logger.fatal("Невалидная дата: " + finishTimeString);
            throw new IllegalArgumentException("Невалидная дата");
        }
        Date finishDate = parseDate(finishTimeString);

        System.out.println("от " + new SimpleDateFormat().format(startDate) + " до " + new SimpleDateFormat().format(finishDate));

        try {
            if (!Files.isDirectory(Path.of(input))) {
                logger.fatal("Неверный путь к директории: " + input);
                throw new IllegalArgumentException("Неверный путь к директории");
            }
            listPath = Files.walk(Path.of(input)).filter(Files::isRegularFile).collect(Collectors.toList());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        listPath.forEach(p -> {
            listMachine.add(new MachineLog(p.toString(), readLog(p)));
        });

        listMachine.forEach(m -> {
            toFile(m.getNumberMachine(), m.getInfo(startDate, finishDate));
        });


    }

    public static boolean isValidDate(String str) {
        String reg = "\\d{2}.\\d{2}.\\d{4}";
        Pattern pattern = Pattern.compile(reg);
        Matcher m = pattern.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }

    public static String readLog(Path path) {
        StringBuilder builder = new StringBuilder();
        try {
            List<String> lines = Files.readAllLines(path);
            for (String line : lines) {
                builder.append(line).append("\n");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return builder.toString();
    }

    public static Date parseDate(String date) {
        Date datePoint = new Date();
        try {
            SimpleDateFormat format = new SimpleDateFormat();
            format.applyPattern("dd.MM.yyyy");
            datePoint = format.parse(date);
        } catch (ParseException ex) {
            System.out.println("некоректная");
            ex.printStackTrace();
        }
        datePoint.setMinutes(0);
        return datePoint;
    }

    public static void toFile(String nameLog, String log) {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(nameLog));
            writer.write(log);
            writer.flush();
            writer.close();
        } catch (Exception e) {
            e.toString();
        }
    }


}

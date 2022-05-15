import java.text.DateFormat;
import java.util.*;
import lombok.Getter;
import java.text.SimpleDateFormat;

public class MachineLog {

    @Getter
    private final String numberMachine;
    @Getter
    private final String log;
    @Getter
    private String OEE;

    private final DateFormat FORMAT_TIME_DATE = new SimpleDateFormat("E dd.MM.yyyy");
    private final DateFormat FORMAT_TIME_HOUR = new SimpleDateFormat("HH");

    private Calendar calendarStart = new GregorianCalendar();
    private Calendar calendarFinish = new GregorianCalendar();
    private Calendar calendarPoint = new GregorianCalendar();//отсечка даты
    private Calendar calendarNow = new GregorianCalendar();//время с логов в нашем поясе
    private Date startProgram = new Date();
    private Date finishProgram = new Date();

    private String pointProgram = "";
    private int countProgramInHours;
    private int timeWorkMachine;

    public MachineLog(String number, String log) {
        this.numberMachine = number;
        this.log = log;
    }

    public String getInfo(Date start, Date finish) {
        StringBuilder sb = new StringBuilder();
        calendarStart.setTime(start);
        calendarFinish.setTime(finish);

        sb.append("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++" + "\n");
        sb.append(numberMachine).append("\n").append(FORMAT_TIME_DATE.format(start.getTime()));

        String[] line = log.split("\n");
        for (String l : line) {
            String[] column = l.split(" ");

            if (column.length <= 1) {
                sb.append("Невалидная строка");
                continue;
            }

            String dateString = column[1].replace('-', '.');
            String time = column[2].replace(":", ".").replaceAll("[a-zA-z].+[a-zA-z]", "");
            dateString += "." + time;
            Date date = parseDate(dateString.replaceAll("\\|", "").trim());
            calendarNow.setTime(date);
            calendarNow.add(Calendar.HOUR_OF_DAY, -5);//Логи записаны по Пекинскому времени

            if (l.contains("start")) {
                startProgram = parseDate(dateString);
            }
            if (l.contains("successfully") || l.contains("forced")) {
                finishProgram = parseDate(dateString);
            }

            if (calendarNow.getTime().compareTo(calendarStart.getTime()) == 1 && calendarNow.getTime().compareTo(calendarFinish.getTime()) < 1) {//нужный диапазон дат
                if (calendarNow.get(Calendar.DATE) != calendarPoint.get(Calendar.DATE)) {//дни
                    calendarPoint.setTime(calendarNow.getTime());
                    sb.append("\t" + "\t" + "\t").append(countProgramInHours).append(" - листов в час").append("\n");
                    countProgramInHours = 0;
                    sb.append(timeWorkMachine).append(" - время работы станка в минутах").append("\n");
                    timeWorkMachine = 0;
                    sb.append(FORMAT_TIME_DATE.format(calendarPoint.getTime())).append("\n");
                    sb.append("\t").append(FORMAT_TIME_HOUR.format(calendarPoint.getTime())).append(" ч.").append("\n");
                }

                if (calendarNow.get(Calendar.HOUR) != calendarPoint.get(Calendar.HOUR)) {//часы
                    calendarPoint.setTime(calendarNow.getTime());
                    sb.append("\t" + "\t" + "\t").append(countProgramInHours).append(" - листов в час").append("\n");
                    countProgramInHours = 0;
                    sb.append("\t").append(FORMAT_TIME_HOUR.format(calendarPoint.getTime())).append(" ч.").append("\n");
                }

                if ((l.contains("successfully") || l.contains("forced")) && pointProgram.compareTo(l) != 1) {//программы
                    pointProgram = l;
                    long timeProgram = (finishProgram.getTime() - startProgram.getTime()) / 60000;
                    timeWorkMachine += (int) timeProgram;
                    countProgramInHours++;
                    String nameProgram = l.substring(l.lastIndexOf('\\') + 1, l.lastIndexOf('\''));
                    sb.append("\t" + "\t" + "\t").append(millisecondsToTime(finishProgram.getTime() - startProgram.getTime())).append(" мин.").append(" : ").append(nameProgram).append("\n");
                }
                //все строки в нужном диапазоне
            }
        }
        OEE = sb.toString();
        return sb.toString();
    }


    private Date parseDate(String date) {
        Date datePoint = new Date();
        try {
            SimpleDateFormat format = new SimpleDateFormat();
            format.applyPattern("yyyy.MM.dd.HH.mm.ss");
            datePoint = format.parse(date);
        } catch (Exception ex) {
            ex.toString();
        }
        return datePoint;
    }

    private String millisecondsToTime(long milliseconds) {
        long minutes = (milliseconds / 1000) / 60;
        long seconds = (milliseconds / 1000) % 60;
        String secondsStr = Long.toString(seconds);
        String secs;
        if (secondsStr.length() >= 2) {
            secs = secondsStr.substring(0, 2);
        } else {
            secs = "0" + secondsStr;
        }

        return minutes + ":" + secs;
    }

}

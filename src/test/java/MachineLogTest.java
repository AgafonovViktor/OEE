import java.nio.file.Path;
import java.util.Date;
import junit.framework.TestCase;

public class MachineLogTest extends TestCase{

    private MachineLog machineLog;
    private String path = "src/test/resources/log.txt";
    private Date start;
    private Date finish;

    @Override
    protected void setUp() throws Exception {
        start = Main.parseDate("26.10.2021");
        finish = Main.parseDate("28.10.2021");
        machineLog = new MachineLog(path, Main.readLog(Path.of(path)));
    }

    public void testGetInfo(){
        String actual = machineLog.getInfo(start, finish);
        String expected = Main.readLog(Path.of("src/test/resources/result.txt"));
        assertEquals(expected, actual);
    }

}

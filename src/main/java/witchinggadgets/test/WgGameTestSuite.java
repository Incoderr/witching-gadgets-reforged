package witchinggadgets.test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import witchinggadgets.WitchingGadgets;

@GameTestHolder(WitchingGadgets.MODID)
@PrefixGameTestTemplate(false)
public final class WgGameTestSuite {
    private static final List<WgGameTestSuite> SUITES = new ArrayList<>();
    private final String name;
    private final List<TestCase> tests = new ArrayList<>();
    private boolean isolated;

    private record TestCase(String name, Consumer<GameTestHelper> body) {
    }

    public WgGameTestSuite(String name) {
        this.name = name;
    }

    public WgGameTestSuite add(String test, Consumer<GameTestHelper> body) {
        tests.add(new TestCase(name + "_" + test, body));
        return this;
    }

    public WgGameTestSuite isolated() {
        isolated = true;
        return this;
    }

    public void register(IEventBus bus) {
        SUITES.add(this);
    }

    @GameTestGenerator
    public static List<TestFunction> generate() {
        List<TestFunction> result = new ArrayList<>();
        for (WgGameTestSuite suite : SUITES) {
            for (TestCase test : suite.tests) {
                result.add(new TestFunction("witchinggadgets_" + (suite.isolated ? test.name() : suite.name), "witchinggadgets." + test.name(), "witchinggadgets:test/platform", 100, 0, true, helper -> test.body().accept(new WgGameTestHelper(helper))));
            }
        }
        return result;
    }
}

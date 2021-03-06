package freifunk.bremen.de.mobilemeshviewer.gateway;

import com.google.common.base.Optional;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.robolectric.shadows.ShadowApplication;

import javax.inject.Inject;

import freifunk.bremen.de.mobilemeshviewer.Checkable;
import freifunk.bremen.de.mobilemeshviewer.PreferenceController;
import freifunk.bremen.de.mobilemeshviewer.RobolectricTest;
import freifunk.bremen.de.mobilemeshviewer.api.manager.RetrofitServiceManager;
import freifunk.bremen.de.mobilemeshviewer.event.NodeDetailFoundEvent;
import freifunk.bremen.de.mobilemeshviewer.event.NodeDetailNotFoundEvent;
import freifunk.bremen.de.mobilemeshviewer.node.NodeChecker;
import freifunk.bremen.de.mobilemeshviewer.node.model.detail.NodeDetail;
import freifunk.bremen.de.mobilemeshviewer.node.model.simple.Node;

import static org.assertj.core.api.Assertions.assertThat;

public class GatewayNodeDetailLoaderTest extends RobolectricTest {

    @Inject
    GatewayNodeDetailLoader classUnderTest;
    @Mock
    private NodeChecker nodeChecker;
    private Optional<NodeDetail> result = Optional.absent();
    private boolean verify = Boolean.FALSE;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        EventBus.getDefault().register(this);
    }

    @After
    public void tearDown() {
        EventBus.getDefault().unregister(this);
    }

    @Test
    public void testDoInBackgroundNotFound() throws Exception {
        // Given
        final String gatewayName = "testName";
        Mockito.when(nodeChecker.getGatewayByName(gatewayName)).thenReturn(Optional.<Node>absent());

        // When
        classUnderTest.execute(gatewayName);
        ShadowApplication.runBackgroundTasks();

        // Then
        Mockito.verify(nodeChecker).getGatewayByName(gatewayName);
        assertThat(verify).isTrue();
        assertThat(result.isPresent()).isFalse();
    }

    @Test
    public void testDoInBackgroundFound() throws Exception {
        // Given
        final String gatewayName = "testName";
        final String gatewayDetailId = "testID";
        final Node node = Mockito.mock(Node.class);
        final Optional<Node> nodeOptional = Optional.of(node);
        final NodeDetail nodeDetail = Mockito.mock(NodeDetail.class);
        final Optional<NodeDetail> testOptioanl = Optional.of(nodeDetail);

        Mockito.when(nodeChecker.getGatewayByName(gatewayName)).thenReturn(nodeOptional);
        Mockito.when(nodeChecker.getDetailNodeById(gatewayDetailId)).thenReturn(testOptioanl);
        Mockito.when(node.getId()).thenReturn(gatewayDetailId);

        // When
        classUnderTest.execute(gatewayName);
        ShadowApplication.runBackgroundTasks();

        // Then
        Mockito.verify(nodeChecker).getGatewayByName(gatewayName);
        assertThat(verify).isTrue();
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get()).isEqualTo(nodeDetail);
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onNodeDetailFound(NodeDetailFoundEvent event) {
        result = Optional.of(event.getNode());
        verify = Boolean.TRUE;
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onNodeDetailNotFound(NodeDetailNotFoundEvent ignored) {
        verify = Boolean.TRUE;
    }

    @Override
    public TestModule getModuleForInjection() {
        return new CustomTestModule();
    }

    @Override
    public void inject() {
        component.inject(this);
    }

    private class CustomTestModule extends TestModule {
        @Override
        public Checkable<Node> providesNodeChecker(PreferenceController preferenceController, RetrofitServiceManager retrofitServiceManager) {
            return nodeChecker;
        }
    }
}
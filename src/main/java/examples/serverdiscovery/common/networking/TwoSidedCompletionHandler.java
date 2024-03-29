package examples.serverdiscovery.common.networking;

import com.ibm.disni.verbs.IbvWC;
import examples.serverdiscovery.common.DiscoveryRpcPacket;
import jarg.jrcm.networking.dependencies.netrequests.AbstractWorkCompletionHandler;
import jarg.jrcm.networking.dependencies.netrequests.WorkRequestProxy;
import jarg.jrcm.networking.dependencies.netrequests.WorkRequestProxyProvider;
import jarg.jrcm.networking.dependencies.netrequests.types.WorkRequestType;
import jarg.jrcm.rpc.packets.PacketDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles completion events of networking requests to the NIC. Such events
 * can be triggered for example when a message is received, when a message has
 * been sent or when an error occurred, etc.
 */
public class TwoSidedCompletionHandler extends AbstractWorkCompletionHandler {
    private static final Logger logger = LoggerFactory.getLogger(TwoSidedCompletionHandler.class);

    private PacketDispatcher<DiscoveryRpcPacket> packetPacketDispatcher;

    public TwoSidedCompletionHandler(WorkRequestProxyProvider proxyProvider,
                                     PacketDispatcher<DiscoveryRpcPacket> packetPacketDispatcher) {
        super(proxyProvider);
        this.packetPacketDispatcher = packetPacketDispatcher;
    }

    @Override
    public void handleCqEvent(IbvWC workCompletionEvent) {
        // associate event with a Work Request
        WorkRequestProxy workRequestProxy = getProxyProvider().getWorkRequestProxyForWc(workCompletionEvent);
        // if this is a completion for an event other than a RECEIVE, free the request
        if(!workRequestProxy.getWorkRequestType().equals(WorkRequestType.TWO_SIDED_RECV)){
            workRequestProxy.releaseWorkRequest();
        }else{
            // create a packet from the Work Request
            DiscoveryRpcPacket packet = new DiscoveryRpcPacket(workRequestProxy);
            // send packet to packet dispatcher
            packetPacketDispatcher.dispatchPacket(packet);
        }
    }

    @Override
    public void handleCqEventError(IbvWC workCompletionEvent) {
        // associate event with a Work Request
        WorkRequestProxy workRequestProxy = getProxyProvider().getWorkRequestProxyForWc(workCompletionEvent);
        // Must free the request
        workRequestProxy.releaseWorkRequest();
        // Status 5 can happen on remote side disconnect, since we have already posted
        // RECV requests for that remote side.
        if(workCompletionEvent.getStatus() != IbvWC.IbvWcStatus.IBV_WC_WR_FLUSH_ERR.ordinal()){
            logger.error("Error in network request "+ workCompletionEvent.getWr_id()
                    + " of status : " + workCompletionEvent.getStatus());
        }
    }

    public PacketDispatcher<DiscoveryRpcPacket> getPacketPacketDispatcher() {
        return packetPacketDispatcher;
    }
}

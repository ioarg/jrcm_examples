package examples.serverdiscovery.common;

import jarg.jrcm.networking.dependencies.netrequests.WorkRequestProxy;
import jarg.jrcm.rpc.packets.RpcPacketFactory;
import jarg.jrcm.rpc.request.RequestIdGenerator;

/**
 * A factory for server discovery RPC packets.
 */
public class DiscoveryRpcPacketFactory implements RpcPacketFactory<DiscoveryRpcPacket> {

    private RequestIdGenerator<Long> requestIdGenerator;

    public DiscoveryRpcPacketFactory(RequestIdGenerator<Long> requestIdGenerator) {
        this.requestIdGenerator = requestIdGenerator;
    }

    @Override
    public DiscoveryRpcPacket generatePacket(WorkRequestProxy workRequestProxy,
                                             byte messageType, int operationType) {
        // Create the RPC packet headers
        DiscoveryRpcPacketHeaders packetHeaders = new DiscoveryRpcPacketHeaders(workRequestProxy);
        packetHeaders.setMessageType(messageType)
                .setOperationType(operationType)
                .setOperationId(requestIdGenerator.generateRequestId())
                .setPacketNumber(0);
        // Create RPC packet
        return new DiscoveryRpcPacket(packetHeaders, workRequestProxy);
    }
}

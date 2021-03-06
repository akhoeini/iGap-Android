/*
* This is the source code of iGap for Android
* It is licensed under GNU AGPL v3.0
* You should have received a copy of the license in this archive (see LICENSE).
* Copyright © 2017 , iGap - www.iGap.net
* iGap Messenger | Free, Fast and Secure instant messaging application
* The idea of the RooyeKhat Media Company - www.RooyeKhat.co
* All rights reserved.
*/

package net.iGap.response;

import net.iGap.G;
import net.iGap.helper.HelperMember;
import net.iGap.module.enums.ChannelChatRole;
import net.iGap.proto.ProtoChannelAddAdmin;
import net.iGap.proto.ProtoError;

public class ChannelAddAdminResponse extends MessageHandler {

    public int actionId;
    public Object message;
    public String identity;

    public ChannelAddAdminResponse(int actionId, Object protoClass, String identity) {
        super(actionId, protoClass, identity);

        this.message = protoClass;
        this.actionId = actionId;
        this.identity = identity;
    }

    @Override
    public void handler() {
        super.handler();
        ProtoChannelAddAdmin.ChannelAddAdminResponse.Builder builder = (ProtoChannelAddAdmin.ChannelAddAdminResponse.Builder) message;
        HelperMember.updateRole(builder.getRoomId(), builder.getMemberId(), ChannelChatRole.ADMIN.toString());
        //fastAdapter
        //if (G.onChannelAddAdmin != null) {
        //    G.onChannelAddAdmin.onChannelAddAdmin(builder.getRoomId(), builder.getMemberId());
        //}
    }

    @Override
    public void timeOut() {
        super.timeOut();

        if (G.onChannelAddAdmin != null) {
            G.onChannelAddAdmin.onTimeOut();
        }
    }

    @Override
    public void error() {
        super.error();

        ProtoError.ErrorResponse.Builder errorResponse = (ProtoError.ErrorResponse.Builder) message;
        int majorCode = errorResponse.getMajorCode();
        int minorCode = errorResponse.getMinorCode();

        if (G.onChannelAddAdmin != null) {
            G.onChannelAddAdmin.onError(majorCode, minorCode);
        }
    }
}



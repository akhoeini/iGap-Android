/*
* This is the source code of iGap for Android
* It is licensed under GNU AGPL v3.0
* You should have received a copy of the license in this archive (see LICENSE).
* Copyright © 2017 , iGap - www.iGap.net
* iGap Messenger | Free, Fast and Secure instant messaging application
* The idea of the RooyeKhat Media Company - www.RooyeKhat.co
* All rights reserved.
*/

package net.iGap.request;

import net.iGap.proto.ProtoSignalingAccept;

public class RequestSignalingAccept {

    public void signalingAccept(String called_sdp) {

        ProtoSignalingAccept.SignalingAccept.Builder builder = ProtoSignalingAccept.SignalingAccept.newBuilder();
        builder.setCalledSdp(called_sdp);

        RequestWrapper requestWrapper = new RequestWrapper(903, builder);
        try {
            RequestQueue.sendRequest(requestWrapper);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}

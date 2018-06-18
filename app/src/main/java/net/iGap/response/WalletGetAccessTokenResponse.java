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

import android.util.Log;

import net.iGap.eventbus.EventManager;
import net.iGap.eventbus.socketMessages;
import net.iGap.proto.ProtoError;
import net.iGap.proto.ProtoWalletGetAccessToken;

import ir.radsense.raadcore.model.Auth;

public class WalletGetAccessTokenResponse extends MessageHandler {

    public int actionId;
    public Object message;
    public String identity;

    public WalletGetAccessTokenResponse(int actionId, Object protoClass, String identity) {
        super(actionId, protoClass, identity);

        this.message = protoClass;
        this.actionId = actionId;
        this.identity = identity;
    }

    @Override
    public void handler() {
        super.handler();
        ProtoWalletGetAccessToken.WalletGetAccessTokenResponse.Builder builder = (ProtoWalletGetAccessToken.WalletGetAccessTokenResponse.Builder) message;
        builder.getTokenType();
        builder.getAccessToken();
        builder.getExpiresIn();
        Auth auth = new Auth(builder.getAccessToken(), "bearer");
        if (auth.getJWT() == null) {
            return;
        }
        auth.save();
        EventManager.getInstance().postEvent(EventManager.ON_ACCESS_TOKEN_RECIVE, socketMessages.SUCCESS);
        Log.i("TTT", "handler: builder.getTokenType():" + builder.getTokenType() + "   ||   builder.getAccessToken(): " + builder.getAccessToken() + "   ||   builder.getExpiresIn():" + builder.getExpiresIn());
    }

    @Override
    public void timeOut() {
        super.timeOut();
        EventManager.getInstance().postEvent(EventManager.ON_ACCESS_TOKEN_RECIVE, socketMessages.FAILED);

    }

    @Override
    public void error() {
        super.error();
        ProtoError.ErrorResponse.Builder errorResponse = (ProtoError.ErrorResponse.Builder) message;
        int majorCode = errorResponse.getMajorCode();
        int minorCode = errorResponse.getMinorCode();
        EventManager.getInstance().postEvent(EventManager.ON_ACCESS_TOKEN_RECIVE, socketMessages.FAILED);

    }
}



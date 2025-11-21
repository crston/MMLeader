package com.gmail.bobason01;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

public class PlayerInfoProvider {

    public String getLuckPermsPrefix(String name) {
        try {
            if (name == null) {
                return "";
            }
            User user = LuckPermsProvider.get().getUserManager().getUser(name);
            if (user == null) {
                return "";
            }
            String prefix = user.getCachedData().getMetaData().getPrefix();
            if (prefix == null) {
                return "";
            }
            return prefix;
        } catch (Throwable t) {
            return "";
        }
    }

    public String getLuckPermsSuffix(String name) {
        try {
            if (name == null) {
                return "";
            }
            User user = LuckPermsProvider.get().getUserManager().getUser(name);
            if (user == null) {
                return "";
            }
            String suffix = user.getCachedData().getMetaData().getSuffix();
            if (suffix == null) {
                return "";
            }
            return suffix;
        } catch (Throwable t) {
            return "";
        }
    }
}

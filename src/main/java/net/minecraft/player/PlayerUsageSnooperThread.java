package net.minecraft.player;

import net.minecraft.utils.HttpUtil;

import java.util.HashMap;
import java.util.TimerTask;

class PlayerUsageSnooperThread extends TimerTask {
    /**
     * The PlayerUsageSnooper object.
     */
    final PlayerUsageSnooper snooper;

    PlayerUsageSnooperThread(PlayerUsageSnooper par1PlayerUsageSnooper) {
        this.snooper = par1PlayerUsageSnooper;
    }

    public void run() {
        if (PlayerUsageSnooper.getStatsCollectorFor(this.snooper).isSnooperEnabled()) {
            HashMap<String, Object> var1;

            synchronized (PlayerUsageSnooper.getSyncLockFor(this.snooper)) {
                var1 = new HashMap<String, Object>(PlayerUsageSnooper.getDataMapFor(this.snooper));
                var1.put("snooper_count", PlayerUsageSnooper.getSelfCounterFor(this.snooper));
            }

            HttpUtil.sendPost(PlayerUsageSnooper.getStatsCollectorFor(this.snooper).getLogAgent(), PlayerUsageSnooper.getServerUrlFor(this.snooper), var1, true);
        }
    }
}

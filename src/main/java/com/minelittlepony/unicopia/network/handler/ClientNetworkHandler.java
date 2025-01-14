package com.minelittlepony.unicopia.network.handler;

import com.minelittlepony.unicopia.network.MsgBlockDestruction;
import com.minelittlepony.unicopia.network.MsgCancelPlayerAbility;
import com.minelittlepony.unicopia.network.MsgSpawnProjectile;
import com.minelittlepony.unicopia.network.MsgTribeSelect;
import com.minelittlepony.unicopia.network.MsgUnlockTraits;

public interface ClientNetworkHandler {

    void handleTribeScreen(MsgTribeSelect packet);

    void handleSpawnProjectile(MsgSpawnProjectile packet);

    void handleBlockDestruction(MsgBlockDestruction packet);

    void handleCancelAbility(MsgCancelPlayerAbility packet);

    void handleUnlockTraits(MsgUnlockTraits packet);
}

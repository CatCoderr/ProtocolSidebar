package me.catcoder.sidebar.protocol;

import com.google.common.base.Preconditions;
import me.catcoder.sidebar.util.buffer.NetOutput;

import java.util.function.BiConsumer;

public enum ScoreNumberFormat implements BiConsumer<NetOutput, String> {

    BLANK {
        @Override
        public void accept(NetOutput out, String value) {
            out.writeVarInt(0);
        }
    },
    STYLED {
        @Override
        public void accept(NetOutput netOutput, String value) {
            netOutput.writeVarInt(1);
            Preconditions.checkArgument(value != null, "Value cannot be null for STYLED format");
            netOutput.writeComponent(value);
        }
    },
    FIXED {
        @Override
        public void accept(NetOutput out, String value) {
            out.writeVarInt(2);
            Preconditions.checkArgument(value != null, "Value cannot be null for FIXED format");
            out.writeComponent(value);
        }
    }
}

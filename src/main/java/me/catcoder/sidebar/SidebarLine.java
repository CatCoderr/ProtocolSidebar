package me.catcoder.sidebar;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import java.util.Collections;
import java.util.Iterator;
import lombok.Getter;
import lombok.NonNull;
import me.catcoder.sidebar.wrapper.AbstractPacket;
import me.catcoder.sidebar.wrapper.WrapperPlayServerScoreboardScore;
import me.catcoder.sidebar.wrapper.WrapperPlayServerScoreboardTeam;
import org.bukkit.ChatColor;


@Getter
public class SidebarLine {

  private final int index;
  private String text;
  private final Sidebar sidebar;

  SidebarLine(int index, @NonNull String text, @NonNull Sidebar sidebar) {
    this.index = index;
    this.text = text;
    this.sidebar = sidebar;
    show();
  }

  public void setText(@NonNull String text) {
    this.text = text;
    AbstractPacket teamPacket = getTeamPacket(WrapperPlayServerScoreboardTeam.Mode.TEAM_UPDATED);
    AbstractPacket scorePacket = getScorePacket(EnumWrappers.ScoreboardAction.CHANGE);
    sidebar.broadcastPacket(teamPacket);
    sidebar.broadcastPacket(scorePacket);
  }

  public void hide() {
    AbstractPacket teamPacket = getTeamPacket(WrapperPlayServerScoreboardTeam.Mode.TEAM_REMOVED);
    AbstractPacket scorePacket = getScorePacket(EnumWrappers.ScoreboardAction.REMOVE);
    sidebar.broadcastPacket(teamPacket);
    sidebar.broadcastPacket(scorePacket);
  }

  public void show() {
    AbstractPacket teamPacket = getTeamPacket(WrapperPlayServerScoreboardTeam.Mode.TEAM_CREATED);
    AbstractPacket scorePacket = getScorePacket(EnumWrappers.ScoreboardAction.CHANGE);
    sidebar.broadcastPacket(teamPacket);
    sidebar.broadcastPacket(scorePacket);
  }

  AbstractPacket getTeamPacket(int mode) {
    Preconditions.checkNotNull(text, "Text cannot be null");
    Preconditions.checkArgument(text.length() <= 32, "Text length must be <= 32, no otherwise.");
    //Чтобы не дубрировались строки
    String result = ChatColor.values()[index].toString();
    WrapperPlayServerScoreboardTeam team = new WrapperPlayServerScoreboardTeam();
    Iterator<String> iterator = Splitter.fixedLength(16).split(text).iterator();
    String prefix = iterator.next();

    team.setName("text-" + index);
    team.setMode(mode);
    team.setPrefix(prefix);

    team.setPlayers(Collections.singletonList(result));

    //Магия, не трогать
    if (text.length() > 16) {
      String prefixColor = ChatColor.getLastColors(prefix);
      String suffix = iterator.next();

      if (prefix.endsWith(String.valueOf(ChatColor.COLOR_CHAR))) {
        prefix = prefix.substring(0, prefix.length() - 1);
        team.setPrefix(prefix);
        prefixColor = ChatColor.getByChar(suffix.charAt(0)).toString();
        suffix = suffix.substring(1);
      }

      if (prefixColor == null) {
        prefixColor = "";
      }

      if (suffix.length() > 16) {
        suffix = suffix.substring(0, (13 - prefixColor.length()));
      }

      team.setSuffix((prefixColor.equals("") ? ChatColor.RESET : prefixColor) + suffix);
    }

    return team;
  }

  AbstractPacket getScorePacket(EnumWrappers.ScoreboardAction action) {
    WrapperPlayServerScoreboardScore score = new WrapperPlayServerScoreboardScore();
    //Прказываем/удаляем линию
    score.setObjectiveName(sidebar.getObjectiveName());
    score.setScoreboardAction(action);
    score.setValue(index);
    score.setScoreName(ChatColor.values()[index].toString());

    return score;
  }


}

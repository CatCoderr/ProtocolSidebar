package me.catcoder.sidebar;

import com.comphenix.protocol.wrappers.EnumWrappers;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.NonNull;
import me.catcoder.sidebar.wrapper.AbstractPacket;
import me.catcoder.sidebar.wrapper.WrapperPlayServerScoreboardTeam;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Класс скорборда.
 *
 * @author CatCoder
 */
public class Sidebar {

    private final HashSet<Player> players = Sets.newHashSet();
    private final Map<Integer, SidebarLine> lines = Maps.newHashMap();

    @Getter
    private SidebarObjective objective;

    /**
     * Установка objective, если предыдущая objective здесь существует,
     * то она удаляется.
     *
     * @param objective - objective
     */
    public void setObjective(@NonNull final SidebarObjective objective) {
        this.objective = objective;
        for (Player player : unregisterForAll()) { //отправляем новую objective
            send(player);
        }
        recreate(); //пересоздаем линии.
    }

    /**
     * Установка линии на текущей objective.
     *
     * @param index - индекс
     * @param text  - текст линии
     */
    public void setLine(int index, String text) {
        SidebarLine line = getLine(index);
        if (line == null) lines.put(index, new SidebarLine(index, text, this));
        else line.setText(text);
    }

    /**
     * Получение определенной линии.
     *
     * @param index - индекс линии
     * @return линию, которая соответствует данному индексу, иначе - null.
     */
    public SidebarLine getLine(int index) {
        return lines.get(index);
    }

    /**
     * Возвращает неизменяемую коллекцию игроков, которые обрабатывают этот sidebar
     *
     * @return коллекцию игроков
     */
    public Set<Player> getPlayers() {
        return Collections.unmodifiableSet(players);
    }

    /**
     * Возвращает неизменяемую коллекцию линий.
     *
     * @return коллекцию линий
     */
    public Map<Integer, SidebarLine> getLines() {
        return Collections.unmodifiableMap(lines);
    }

    /**
     * Вспомагательный метод, который отправляет пакет всем игрокам из Sidebar::getPlayers
     *
     * @param packet - пакет, который нужно отправить
     */
    protected void broadcastPacket(@NonNull AbstractPacket packet) {
        players.forEach(packet::sendPacket);
    }

    /**
     * Удаление objective для определенного игрока
     *
     * @param player - игрок, которому нужно удалить sidebar.
     */
    public void unregister(@NonNull Player player) {
        Preconditions.checkState(players.contains(player), "Player %s is not receiving this sidebar.", player.getName());
        lines.values().forEach(line -> {
            line.getTeamPacket(WrapperPlayServerScoreboardTeam.Mode.TEAM_REMOVED).sendPacket(player);
            line.getScorePacket(EnumWrappers.ScoreboardAction.REMOVE).sendPacket(player);
        });
        objective.remove(player);
        players.remove(player);
    }

    /**
     * Удаление objective для всех игроков в Sidebar::getPlayers.
     *
     * @return клон коллекции игроков
     */
    public Collection<Player> unregisterForAll() {
        HashSet<Player> cloned = (HashSet<Player>) players.clone();
        players.forEach(this::unregister);

        return cloned;
    }

    /**
     * Переустановка всех линий.
     */
    private void recreate() {
        lines.values().forEach(SidebarLine::show);
    }


    /**
     * Отправка sidebar игрокам
     *
     * @param players - массив игроков
     */
    public void send(@NonNull Player... players) {
        for (Player player : players) {
            Preconditions.checkArgument(!this.players.contains(player), "Player %s already receiving this sidebar.", player.getName());
            objective.create(player);
            lines.values().forEach(line -> {
                line.getTeamPacket(WrapperPlayServerScoreboardTeam.Mode.TEAM_CREATED).sendPacket(player);
                line.getScorePacket(EnumWrappers.ScoreboardAction.CHANGE).sendPacket(player);
            });
            objective.show(player);
        }
        Collections.addAll(this.players, players);
    }
}

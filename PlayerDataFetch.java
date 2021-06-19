import java.util.*;
import java.util.stream.Collectors;

public class PlayerDataFetch {
    private Set<PlayerDetail> playerDetailList = new HashSet<>();

    public void addPlayerDetail(String name, short team, short position, float credits, short preference, float projectedPoints) {
        playerDetailList.add(new PlayerDetail(name, team, credits, preference, position,projectedPoints));
    }

    public Set<PlayerDetail> getPlayerDetailList() {
        return playerDetailList;
    }

    public List<PlayerDetail> getSortedPlayerDetailSet() {
        if (!playerDetailList.isEmpty()) {
            return playerDetailList.stream().sorted(Comparator.comparing(PlayerDetail::getPriority)).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}

class PlayerDetail {
    private String name;
    private float credits;
    private short team; //{0,1};
    private short priority;
    private short position;
    private float projectedPoints;

    public PlayerDetail(String name, short team, float credits, short priority, short position, float projectedPoints) {
        this.name = name;
        this.credits = credits;
        this.priority = priority;
        this.position = position;
        this.team = team;
        this.projectedPoints = projectedPoints;
    }

    public String getName() {
        return name;
    }

    public float getCredits() {
        return credits;
    }

    public short getPriority() {
        return priority;
    }

    public short getPosition() {
        return position;
    }

    public short getTeam() {
        return team;
    }

    public float getProjectedPoints() {
        return projectedPoints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerDetail that = (PlayerDetail) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}


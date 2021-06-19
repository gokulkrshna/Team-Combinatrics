import org.supercsv.cellprocessor.ConvertNullTo;
import org.supercsv.cellprocessor.ParseDouble;
import org.supercsv.cellprocessor.ParseInt;
import org.supercsv.cellprocessor.constraint.StrNotNullOrEmpty;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import java.io.File;
import java.io.FileReader;
import java.util.*;

public class DreamHackImpl {

    private static final String[] colNames = new String[]{"player_name", "team", "position", "credits", "preference", "projected_points"};


    private static CellProcessor[] getProcessors() {
        return new CellProcessor[]{
                new StrNotNullOrEmpty(),//player_name
                new ParseInt(),//team {'A'/'B'}
                new ParseInt(),//position {0,1,2,3,4}
                new ParseDouble(),//credits
                new ConvertNullTo(999),
                new ConvertNullTo(1)//projected_points
        };
    }

    static void processFile(File file) throws Exception {
        if (file == null) {
            throw new Exception("File object cannot be null!!");
        }
        PlayerDataFetch playerDataFetch = new PlayerDataFetch();
        try (ICsvMapReader icsMapReader = new CsvMapReader(new FileReader(file), CsvPreference.STANDARD_PREFERENCE)) {
            Map<String, Object> read;
            icsMapReader.getHeader(true);
            while ((read = icsMapReader.read(colNames, getProcessors())) != null) {
                playerDataFetch.addPlayerDetail((String) read.get(colNames[0]),
                        Short.parseShort(read.get(colNames[1]).toString()),
                        Short.parseShort(read.get(colNames[2]).toString()),
                        Float.parseFloat(read.get(colNames[3]).toString()),
                        Short.parseShort(read.get(colNames[4]).toString()),
                        Float.parseFloat(read.get(colNames[5]).toString()));
            }
            /*playerDataFetch.addPlayerDetail(read.get(colNames[0]),
                    Short.valueOf(read.get(colNames[1])),
                    Short.valueOf(read.get(colNames[2])),
                    Float.valueOf(read.get(colNames[3])), Short.valueOf(read.get(colNames[4])));*/
        } catch (Exception e) {
            throw new Exception("Error while parsing CSV " + e.getMessage());
        }
        TeamBuilder.generateTeams(playerDataFetch);
        for (int i = 3; i <= TeamBuilder.topPlayersSize; i++) {
            TeamBuilder.combinationUtil(i, 0, 0);
        }
        TeamBuilder.printAllTeamsInSortedOrder();
    }
}

class TeamBuilder {

    private static final short MAX_CREDITS = 100;
    static List<PlayerDetail> topPlayers = new ArrayList<>();
    static List<PlayerDetail> remainingPlayers = new ArrayList<>();
    static int topPlayersSize = 0;
    static int totalRemainingPlayers = 0;
    static int maxPlayerPerTeam = 8;
    static List<Map<String, Object>> validTeams = new ArrayList<>();


    static void generateTeams(PlayerDataFetch playerDataFetch) {
        if (playerDataFetch != null && playerDataFetch.getPlayerDetailList().size() > 0) {
            List<PlayerDetail> playerDetailList = playerDataFetch.getSortedPlayerDetailSet();
            for (PlayerDetail playerDetail : playerDetailList) {
                if (topPlayers.isEmpty() ||
                        topPlayers.get(topPlayers.size() - 1).getPriority() == playerDetail.getPriority() ||
                        topPlayers.size() < 2) {
                    topPlayers.add(playerDetail);
                    topPlayersSize++;
                } else {
                    remainingPlayers.add(playerDetail);
                    totalRemainingPlayers++;
                }
            }
        }
    }

    static void combinationUtil(int maxTopPlayersInTeam, int index, int i) {
        if (index == maxTopPlayersInTeam) {
            //start making team from remaining players
            StringBuilder stringBuilder = new StringBuilder();
            for (int j = 0; j < maxTopPlayersInTeam; j++) {
                if (Team.playerDetails[j] == null) {
                    System.out.print("NULL");
                } else {
                    String s = Team.playerDetails[j].getName() + " ";
                    stringBuilder.append(s);
                    System.out.print(s);
                }
            }
            if (!stringBuilder.isEmpty() && stringBuilder.toString().trim().equals("KM GA BF LS PC NC PJ MJ")) {
                System.out.println("Found ERR!!!");
            }
            System.out.println();
            makeTeamFromRemainingPlayers(maxTopPlayersInTeam, maxTopPlayersInTeam, 0);
            return;
        }

        if (i >= topPlayersSize) {
            return;
        }

        removePlayerAtIdx(index);
        addPlayerToTeam(index, topPlayers.get(i));
//            data[index] = topPlayers.get(i);
        combinationUtil(maxTopPlayersInTeam, index + 1, i + 1);
        combinationUtil(maxTopPlayersInTeam, index, i + 1);
    }

    static void printAllTeamsInSortedOrder() {
        Collections.sort(validTeams, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                return Float.compare((float) o2.get("projected_points"), (float) o1.get("projected_points"));
            }
        });
        int i = 0;
        for (Map<String, Object> validTeam : validTeams) {
            Object projected_points = validTeam.get("projected_points");
            if (i++ > 100) {
                break;
            }
            System.out.println(validTeam.get("team_players") + " " + projected_points);
        }
    }

    static void makeTeamFromRemainingPlayers(int startInd, int index, int i) {
        if (index == maxPlayerPerTeam) {
            StringBuilder stringBuilder = new StringBuilder();
            float projectedTotal = 0;
            if (Arrays.stream(Team.currentTeamPositionCount).anyMatch(c -> c == 0)) {
                return;
            }
            Map<String, Object> tTeam = new HashMap<>();
            for (int j = 0; j < maxPlayerPerTeam; j++) {
                if (Team.playerDetails[j] == null) {
                    System.out.print("NULL");
                } else {
                    String s = Team.playerDetails[j].getName() + " ";
                    stringBuilder.append(s);
//                    System.out.print(s);
                    float projectedPoints = Team.playerDetails[j].getProjectedPoints();
                    projectedTotal += j == 0 ? projectedPoints * 2 : j == 1 ? projectedPoints * 1.5 : projectedPoints;
                }
            }
            tTeam.put("team_players", stringBuilder.toString());
            tTeam.put("projected_points", projectedTotal);
            validTeams.add(tTeam);
//            System.out.print(Team.currentTotal + " ");
//            System.out.println(projectedTotal);
            if (!stringBuilder.isEmpty() && stringBuilder.toString().trim().equals("JM LT JS TH KW TC CS IW")) {
                System.out.println("Found ERR!!!");
            }
//            System.out.println();
            return;
        }

        if (i >= totalRemainingPlayers) {
            return;
        }

        removePlayerAtIdx(index);
        boolean b = addPlayerToTeam(index, remainingPlayers.get(i));
//            Team.playerDetails[index] = remainingPlayers.get(i);
        if (b) {
            makeTeamFromRemainingPlayers(startInd, index + 1, i + 1);
        }
        if (index >= startInd) {
            makeTeamFromRemainingPlayers(startInd, index, i + 1);
        }
    }


    static boolean addPlayerToTeam(int idx, PlayerDetail playerDetail) {
        if (checkIfPlayerCanBeAddedToTeam(playerDetail)) {
            int teamPlayerCount = Team.currentTeamPlayerCount[playerDetail.getTeam()];
            int teamPlayerPositionCount = Team.currentTeamPositionCount[playerDetail.getPosition()];
            Team.currentTotal += playerDetail.getCredits();
            Team.currentTeamPlayerCount[playerDetail.getTeam()] = ++teamPlayerCount;
            Team.currentTeamPositionCount[playerDetail.getPosition()] = ++teamPlayerPositionCount;
            Team.playerDetails[idx] = playerDetail;
            Team.currentTeamIdx++;
                /*if (currentTeamIdx == 8) {
                    //found team, reset
                    validTeams.add(currentTeam);
                }*/
            return true;
        }
        return false;
    }

    static boolean checkIfPlayerCanBeAddedToTeam(PlayerDetail playerDetail) {
        if (Team.currentTeamIdx + 1 <= 8) {
            if (Team.currentTotal + playerDetail.getCredits() <= MAX_CREDITS) {
                int teamPlayerCount = Team.currentTeamPlayerCount[playerDetail.getTeam()];
                if (teamPlayerCount + 1 <= 5) {
                    int teamPlayerPositionCount = Team.currentTeamPositionCount[playerDetail.getPosition()];
                    return teamPlayerPositionCount + 1 <= 4;
                }
            }
        }

        return false;
    }

/*
        static void removeLatestPlayerFromTeam() {
            PlayerDetail playerDetail = currentTeam[--currentTeamIdx];
            if (playerDetail != null) {
                int i = currentTeamPlayerCount[playerDetail.getTeam()];
                currentTeamPlayerCount[playerDetail.getTeam()] = i - 1;
                int i1 = currentTeamPositionCount[playerDetail.getPosition()];
                currentTeamPositionCount[playerDetail.getPosition()] = i1 - 1;
                currentTotal -= playerDetail.getCredits();
                currentTeam[currentTeamIdx] = null;
            }
        }
*/

    static void removePlayerAtIdx(int idx) {
        if (Team.currentTeamIdx >= idx && Team.playerDetails[idx] != null) {
            PlayerDetail playerDetail = Team.playerDetails[idx];
            int i = Team.currentTeamPlayerCount[playerDetail.getTeam()];
            Team.currentTeamPlayerCount[playerDetail.getTeam()] = i - 1;
            int i1 = Team.currentTeamPositionCount[playerDetail.getPosition()];
            Team.currentTeamPositionCount[playerDetail.getPosition()] = i1 - 1;
            Team.currentTotal -= playerDetail.getCredits();
            Team.playerDetails[idx] = null;
            Team.currentTeamIdx--;
            removePlayerAtIdx(idx + 1);
        }
    }
}

class Team {
    static PlayerDetail[] playerDetails = new PlayerDetail[8];
    static int[] currentTeamPositionCount = new int[]{0, 0, 0, 0, 0};
    static int[] currentTeamPlayerCount = new int[]{0, 0};
    static int currentTeamIdx = 0;
    static float currentTotal = 0;
}

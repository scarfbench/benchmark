package quarkus.tutorial.roster.client;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import quarkus.tutorial.roster.util.LeagueDetails;
import quarkus.tutorial.roster.util.PlayerDetails;
import quarkus.tutorial.roster.util.TeamDetails;

import java.util.List;

public class RosterClient {
    private static final String BASE_URI = "http://localhost:8080/roster";

    public RosterClient(String[] args) {
    }

    public static void main(String[] args) {
        RosterClient client = new RosterClient(args);
        try (Client jaxrsClient = ClientBuilder.newClient()) {
            client.insertInfo(jaxrsClient);
            client.getSomeInfo(jaxrsClient);
            client.getMoreInfo(jaxrsClient);
            client.removeInfo(jaxrsClient);
            System.exit(0);
        } catch (Exception ex) {
            System.err.println("Caught an exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void insertInfo(Client client) {
        try {
            // Leagues
            client.target(BASE_URI).path("/league")
                    .request()
                    .post(Entity.json(new LeagueDetails("L1", "Mountain", "Soccer")));
            client.target(BASE_URI).path("/league")
                    .request()
                    .post(Entity.json(new LeagueDetails("L2", "Valley", "Basketball")));
            client.target(BASE_URI).path("/league")
                    .request()
                    .post(Entity.json(new LeagueDetails("L3", "Foothills", "Soccer")));
            client.target(BASE_URI).path("/league")
                    .request()
                    .post(Entity.json(new LeagueDetails("L4", "Alpine", "Snowboarding")));

            // Teams
            client.target(BASE_URI).path("/team/league/L1")
                    .request()
                    .post(Entity.json(new TeamDetails("T1", "Honey Bees", "Visalia")));
            client.target(BASE_URI).path("/team/league/L1")
                    .request()
                    .post(Entity.json(new TeamDetails("T2", "Gophers", "Manteca")));
            client.target(BASE_URI).path("/team/league/L1")
                    .request()
                    .post(Entity.json(new TeamDetails("T5", "Crows", "Orland")));
            client.target(BASE_URI).path("/team/league/L2")
                    .request()
                    .post(Entity.json(new TeamDetails("T3", "Deer", "Bodie")));
            client.target(BASE_URI).path("/team/league/L2")
                    .request()
                    .post(Entity.json(new TeamDetails("T4", "Trout", "Truckee")));
            client.target(BASE_URI).path("/team/league/L3")
                    .request()
                    .post(Entity.json(new TeamDetails("T6", "Marmots", "Auburn")));
            client.target(BASE_URI).path("/team/league/L3")
                    .request()
                    .post(Entity.json(new TeamDetails("T7", "Bobcats", "Grass Valley")));
            client.target(BASE_URI).path("/team/league/L3")
                    .request()
                    .post(Entity.json(new TeamDetails("T8", "Beavers", "Placerville")));
            client.target(BASE_URI).path("/team/league/L4")
                    .request()
                    .post(Entity.json(new TeamDetails("T9", "Penguins", "Incline Village")));
            client.target(BASE_URI).path("/team/league/L4")
                    .request()
                    .post(Entity.json(new TeamDetails("T10", "Land Otters", "Tahoe City")));

            // Players, Team T1
            MultivaluedMap<String, String> playerForm1 = new MultivaluedHashMap<>();
            playerForm1.add("id", "P1");
            playerForm1.add("name", "Phil Jones");
            playerForm1.add("position", "goalkeeper");
            playerForm1.add("salary", "100.00");
            client.target(BASE_URI).path("/player").request()
                    .post(Entity.form(playerForm1));
            client.target(BASE_URI).path("/player/P1/team/T1").request().post(Entity.json(""));

            MultivaluedMap<String, String> playerForm2 = new MultivaluedHashMap<>();
            playerForm2.add("id", "P2");
            playerForm2.add("name", "Alice Smith");
            playerForm2.add("position", "defender");
            playerForm2.add("salary", "505.00");
            client.target(BASE_URI).path("/player").request()
                    .post(Entity.form(playerForm2));
            client.target(BASE_URI).path("/player/P2/team/T1").request().post(Entity.json(""));

            MultivaluedMap<String, String> playerForm3 = new MultivaluedHashMap<>();
            playerForm3.add("id", "P3");
            playerForm3.add("name", "Bob Roberts");
            playerForm3.add("position", "midfielder");
            playerForm3.add("salary", "65.00");
            client.target(BASE_URI).path("/player").request()
                    .post(Entity.form(playerForm3));
            client.target(BASE_URI).path("/player/P3/team/T1").request().post(Entity.json(""));

            MultivaluedMap<String, String> playerForm4 = new MultivaluedHashMap<>();
            playerForm4.add("id", "P4");
            playerForm4.add("name", "Grace Phillips");
            playerForm4.add("position", "forward");
            playerForm4.add("salary", "100.00");
            client.target(BASE_URI).path("/player").request()
                    .post(Entity.form(playerForm4));
            client.target(BASE_URI).path("/player/P4/team/T1").request().post(Entity.json(""));

            MultivaluedMap<String, String> playerForm5 = new MultivaluedHashMap<>();
            playerForm5.add("id", "P5");
            playerForm5.add("name", "Barney Bold");
            playerForm5.add("position", "defender");
            playerForm5.add("salary", "100.00");
            client.target(BASE_URI).path("/player").request()
                    .post(Entity.form(playerForm5));
            client.target(BASE_URI).path("/player/P5/team/T1").request().post(Entity.json(""));

            // Players, Team T2
            MultivaluedMap<String, String> playerForm6 = new MultivaluedHashMap<>();
            playerForm6.add("id", "P6");
            playerForm6.add("name", "Ian Carlyle");
            playerForm6.add("position", "goalkeeper");
            playerForm6.add("salary", "555.00");
            client.target(BASE_URI).path("/player").request()
                    .post(Entity.form(playerForm6));
            client.target(BASE_URI).path("/player/P6/team/T2").request().post(Entity.json(""));

            MultivaluedMap<String, String> playerForm7 = new MultivaluedHashMap<>();
            playerForm7.add("id", "P7");
            playerForm7.add("name", "Rebecca Struthers");
            playerForm7.add("position", "midfielder");
            playerForm7.add("salary", "777.00");
            client.target(BASE_URI).path("/player").request()
                    .post(Entity.form(playerForm7));
            client.target(BASE_URI).path("/player/P7/team/T2").request().post(Entity.json(""));

            MultivaluedMap<String, String> playerForm8 = new MultivaluedHashMap<>();
            playerForm8.add("id", "P8");
            playerForm8.add("name", "Anne Anderson");
            playerForm8.add("position", "forward");
            playerForm8.add("salary", "65.00");
            client.target(BASE_URI).path("/player").request()
                    .post(Entity.form(playerForm8));
            client.target(BASE_URI).path("/player/P8/team/T2").request().post(Entity.json(""));

            MultivaluedMap<String, String> playerForm9 = new MultivaluedHashMap<>();
            playerForm9.add("id", "P9");
            playerForm9.add("name", "Jan Wesley");
            playerForm9.add("position", "defender");
            playerForm9.add("salary", "100.00");
            client.target(BASE_URI).path("/player").request()
                    .post(Entity.form(playerForm9));
            client.target(BASE_URI).path("/player/P9/team/T2").request().post(Entity.json(""));

            MultivaluedMap<String, String> playerForm10 = new MultivaluedHashMap<>();
            playerForm10.add("id", "P10");
            playerForm10.add("name", "Terry Smithson");
            playerForm10.add("position", "midfielder");
            playerForm10.add("salary", "100.00");
            client.target(BASE_URI).path("/player").request()
                    .post(Entity.form(playerForm10));
            client.target(BASE_URI).path("/player/P10/team/T2").request().post(Entity.json(""));

            // Players, Team T3
            MultivaluedMap<String, String> playerForm11 = new MultivaluedHashMap<>();
            playerForm11.add("id", "P11");
            playerForm11.add("name", "Ben Shore");
            playerForm11.add("position", "point guard");
            playerForm11.add("salary", "188.00");
            client.target(BASE_URI).path("/player").request()
                    .post(Entity.form(playerForm11));
            client.target(BASE_URI).path("/player/P11/team/T3").request().post(Entity.json(""));

            MultivaluedMap<String, String> playerForm12 = new MultivaluedHashMap<>();
            playerForm12.add("id", "P12");
            playerForm12.add("name", "Chris Farley");
            playerForm12.add("position", "shooting guard");
            playerForm12.add("salary", "577.00");
            client.target(BASE_URI).path("/player").request()
                    .post(Entity.form(playerForm12));
            client.target(BASE_URI).path("/player/P12/team/T3").request().post(Entity.json(""));

            MultivaluedMap<String, String> playerForm13 = new MultivaluedHashMap<>();
            playerForm13.add("id", "P13");
            playerForm13.add("name", "Audrey Brown");
            playerForm13.add("position", "small forward");
            playerForm13.add("salary", "995.00");
            client.target(BASE_URI).path("/player").request()
                    .post(Entity.form(playerForm13));
            client.target(BASE_URI).path("/player/P13/team/T3").request().post(Entity.json(""));

            MultivaluedMap<String, String> playerForm14 = new MultivaluedHashMap<>();
            playerForm14.add("id", "P14");
            playerForm14.add("name", "Jack Patterson");
            playerForm14.add("position", "power forward");
            playerForm14.add("salary", "100.00");
            client.target(BASE_URI).path("/player").request()
                    .post(Entity.form(playerForm14));
            client.target(BASE_URI).path("/player/P14/team/T3").request().post(Entity.json(""));

            MultivaluedMap<String, String> playerForm15 = new MultivaluedHashMap<>();
            playerForm15.add("id", "P15");
            playerForm15.add("name", "Candace Lewis");
            playerForm15.add("position", "point guard");
            playerForm15.add("salary", "100.00");
            client.target(BASE_URI).path("/player").request()
                    .post(Entity.form(playerForm15));
            client.target(BASE_URI).path("/player/P15/team/T3").request().post(Entity.json(""));

            // Players, Team T4
            MultivaluedMap<String, String> playerForm16 = new MultivaluedHashMap<>();
            playerForm16.add("id", "P16");
            playerForm16.add("name", "Linda Berringer");
            playerForm16.add("position", "point guard");
            playerForm16.add("salary", "844.00");
            client.target(BASE_URI).path("/player").request()
                    .post(Entity.form(playerForm16));
            client.target(BASE_URI).path("/player/P16/team/T4").request().post(Entity.json(""));

            MultivaluedMap<String, String> playerForm17 = new MultivaluedHashMap<>();
            playerForm17.add("id", "P17");
            playerForm17.add("name", "Bertrand Morris");
            playerForm17.add("position", "shooting guard");
            playerForm17.add("salary", "452.00");
            client.target(BASE_URI).path("/player").request()
                    .post(Entity.form(playerForm17));
            client.target(BASE_URI).path("/player/P17/team/T4").request().post(Entity.json(""));

            MultivaluedMap<String, String> playerForm18 = new MultivaluedHashMap<>();
            playerForm18.add("id", "P18");
            playerForm18.add("name", "Nancy White");
            playerForm18.add("position", "small forward");
            playerForm18.add("salary", "833.00");
            client.target(BASE_URI).path("/player").request()
                    .post(Entity.form(playerForm18));
            client.target(BASE_URI).path("/player/P18/team/T4").request().post(Entity.json(""));

            MultivaluedMap<String, String> playerForm19 = new MultivaluedHashMap<>();
            playerForm19.add("id", "P19");
            playerForm19.add("name", "Billy Black");
            playerForm19.add("position", "power forward");
            playerForm19.add("salary", "444.00");
            client.target(BASE_URI).path("/player").request()
                    .post(Entity.form(playerForm19));
            client.target(BASE_URI).path("/player/P19/team/T4").request().post(Entity.json(""));

            MultivaluedMap<String, String> playerForm20 = new MultivaluedHashMap<>();
            playerForm20.add("id", "P20");
            playerForm20.add("name", "Jodie James");
            playerForm20.add("position", "point guard");
            playerForm20.add("salary", "100.00");
            client.target(BASE_URI).path("/player").request()
                    .post(Entity.form(playerForm20));
            client.target(BASE_URI).path("/player/P20/team/T4").request().post(Entity.json(""));

            // Players, Team T5
            MultivaluedMap<String, String> playerForm21 = new MultivaluedHashMap<>();
            playerForm21.add("id", "P21");
            playerForm21.add("name", "Henry Shute");
            playerForm21.add("position", "goalkeeper");
            playerForm21.add("salary", "205.00");
            client.target(BASE_URI).path("/player").request()
                    .post(Entity.form(playerForm21));
            client.target(BASE_URI).path("/player/P21/team/T5").request().post(Entity.json(""));

            MultivaluedMap<String, String> playerForm22 = new MultivaluedHashMap<>();
            playerForm22.add("id", "P22");
            playerForm22.add("name", "Janice Walker");
            playerForm22.add("position", "defender");
            playerForm22.add("salary", "857.00");
            client.target(BASE_URI).path("/player").request()
                    .post(Entity.form(playerForm22));
            client.target(BASE_URI).path("/player/P22/team/T5").request().post(Entity.json(""));

            MultivaluedMap<String, String> playerForm23 = new MultivaluedHashMap<>();
            playerForm23.add("id", "P23");
            playerForm23.add("name", "Wally Hendricks");
            playerForm23.add("position", "midfielder");
            playerForm23.add("salary", "748.00");
            client.target(BASE_URI).path("/player").request()
                    .post(Entity.form(playerForm23));
            client.target(BASE_URI).path("/player/P23/team/T5").request().post(Entity.json(""));

            MultivaluedMap<String, String> playerForm24 = new MultivaluedHashMap<>();
            playerForm24.add("id", "P24");
            playerForm24.add("name", "Gloria Garber");
            playerForm24.add("position", "forward");
            playerForm24.add("salary", "777.00");
            client.target(BASE_URI).path("/player").request()
                    .post(Entity.form(playerForm24));
            client.target(BASE_URI).path("/player/P24/team/T5").request().post(Entity.json(""));

            MultivaluedMap<String, String> playerForm25 = new MultivaluedHashMap<>();
            playerForm25.add("id", "P25");
            playerForm25.add("name", "Frank Fletcher");
            playerForm25.add("position", "defender");
            playerForm25.add("salary", "399.00");
            client.target(BASE_URI).path("/player").request()
                    .post(Entity.form(playerForm25));
            client.target(BASE_URI).path("/player/P25/team/T5").request().post(Entity.json(""));

            // Players, Team T9
            MultivaluedMap<String, String> playerForm30 = new MultivaluedHashMap<>();
            playerForm30.add("id", "P30");
            playerForm30.add("name", "Lakshme Singh");
            playerForm30.add("position", "downhill");
            playerForm30.add("salary", "450.00");
            client.target(BASE_URI).path("/player").request()
                    .post(Entity.form(playerForm30));
            client.target(BASE_URI).path("/player/P30/team/T9").request().post(Entity.json(""));

            MultivaluedMap<String, String> playerForm31 = new MultivaluedHashMap<>();
            playerForm31.add("id", "P31");
            playerForm31.add("name", "Mariela Prieto");
            playerForm31.add("position", "freestyle");
            playerForm31.add("salary", "420.00");
            client.target(BASE_URI).path("/player").request()
                    .post(Entity.form(playerForm31));
            client.target(BASE_URI).path("/player/P31/team/T9").request().post(Entity.json(""));

            // Players, Team T10
            MultivaluedMap<String, String> playerForm32 = new MultivaluedHashMap<>();
            playerForm32.add("id", "P32");
            playerForm32.add("name", "Soren Johannsen");
            playerForm32.add("position", "freestyle");
            playerForm32.add("salary", "375.00");
            client.target(BASE_URI).path("/player").request()
                    .post(Entity.form(playerForm32));
            client.target(BASE_URI).path("/player/P32/team/T10").request().post(Entity.json(""));

            MultivaluedMap<String, String> playerForm33 = new MultivaluedHashMap<>();
            playerForm33.add("id", "P33");
            playerForm33.add("name", "Andre Gerson");
            playerForm33.add("position", "freestyle");
            playerForm33.add("salary", "396.00");
            client.target(BASE_URI).path("/player").request()
                    .post(Entity.form(playerForm33));
            client.target(BASE_URI).path("/player/P33/team/T10").request().post(Entity.json(""));

            MultivaluedMap<String, String> playerForm34 = new MultivaluedHashMap<>();
            playerForm34.add("id", "P34");
            playerForm34.add("name", "Zoria Lepsius");
            playerForm34.add("position", "downhill");
            playerForm34.add("salary", "431.00");
            client.target(BASE_URI).path("/player").request()
                    .post(Entity.form(playerForm34));
            client.target(BASE_URI).path("/player/P34/team/T10").request().post(Entity.json(""));

            // Players, no team
            MultivaluedMap<String, String> playerForm26 = new MultivaluedHashMap<>();
            playerForm26.add("id", "P26");
            playerForm26.add("name", "Hobie Jackson");
            playerForm26.add("position", "pitcher");
            playerForm26.add("salary", "582.00");
            client.target(BASE_URI).path("/player").request()
                    .post(Entity.form(playerForm26));

            MultivaluedMap<String, String> playerForm27 = new MultivaluedHashMap<>();
            playerForm27.add("id", "P27");
            playerForm27.add("name", "Melinda Kendall");
            playerForm27.add("position", "catcher");
            playerForm27.add("salary", "677.00");
            client.target(BASE_URI).path("/player").request()
                    .post(Entity.form(playerForm27));

            // Players, multiple teams
            MultivaluedMap<String, String> playerForm28 = new MultivaluedHashMap<>();
            playerForm28.add("id", "P28");
            playerForm28.add("name", "Constance Adams");
            playerForm28.add("position", "substitute");
            playerForm28.add("salary", "966.00");
            client.target(BASE_URI).path("/player").request()
                    .post(Entity.form(playerForm28));
            client.target(BASE_URI).path("/player/P28/team/T1").request().post(Entity.json(""));
            client.target(BASE_URI).path("/player/P28/team/T3").request().post(Entity.json(""));

            // Adding existing players to second soccer league
            client.target(BASE_URI).path("/player/P24/team/T6").request().post(Entity.json(""));
            client.target(BASE_URI).path("/player/P21/team/T6").request().post(Entity.json(""));
            client.target(BASE_URI).path("/player/P9/team/T6").request().post(Entity.json(""));
            client.target(BASE_URI).path("/player/P7/team/T5").request().post(Entity.json(""));
        } catch (Exception ex) {
            System.err.println("Caught an exception: " + ex.getMessage());
        }
    }

    private void getSomeInfo(Client client) {
        try {
            System.out.println("List all players in team T2:");
            List<PlayerDetails> playerList = client.target(BASE_URI).path("/team/T2/players")
                    .request(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<PlayerDetails>>() {});
            printDetailsList(playerList);
            System.out.println();

            System.out.println("List all teams in league L1:");
            List<TeamDetails> teamList = client.target(BASE_URI).path("/league/L1/teams")
                    .request(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<TeamDetails>>() {});
            printDetailsList(teamList);
            System.out.println();

            System.out.println("List all defenders:");
            playerList = client.target(BASE_URI).path("/players/position/defender")
                    .request(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<PlayerDetails>>() {});
            printDetailsList(playerList);
            System.out.println();

            System.out.println("List the leagues of player P28:");
            List<LeagueDetails> leagueList = client.target(BASE_URI).path("/player/P28/leagues")
                    .request(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<LeagueDetails>>() {});
            printDetailsList(leagueList);
            System.out.println();
        } catch (Exception ex) {
            System.err.println("Caught an exception: " + ex.getMessage());
        }
    }

    private void getMoreInfo(Client client) {
        try {
            System.out.println("Details of league L1:");
            LeagueDetails leagueDetails = client.target(BASE_URI).path("/league/L1")
                    .request(MediaType.APPLICATION_JSON)
                    .get(LeagueDetails.class);
            System.out.println(leagueDetails.toString());
            System.out.println();

            System.out.println("Details of team T3:");
            TeamDetails teamDetails = client.target(BASE_URI).path("/team/T3")
                    .request(MediaType.APPLICATION_JSON)
                    .get(TeamDetails.class);
            System.out.println(teamDetails.toString());
            System.out.println();

            System.out.println("Details of player P20:");
            PlayerDetails playerDetails = client.target(BASE_URI).path("/player/P20")
                    .request(MediaType.APPLICATION_JSON)
                    .get(PlayerDetails.class);
            System.out.println(playerDetails.toString());
            System.out.println();

            System.out.println("List all teams in league L3:");
            List<TeamDetails> teamList = client.target(BASE_URI).path("/league/L3/teams")
                    .request(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<TeamDetails>>() {});
            printDetailsList(teamList);
            System.out.println();

            System.out.println("List all players:");
            List<PlayerDetails> playerList = client.target(BASE_URI).path("/players")
                    .request(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<PlayerDetails>>() {});
            printDetailsList(playerList);
            System.out.println();

            System.out.println("List all players not on a team:");
            playerList = client.target(BASE_URI).path("/players/not-on-team")
                    .request(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<PlayerDetails>>() {});
            printDetailsList(playerList);
            System.out.println();

            System.out.println("Details of Jack Patterson, a power forward:");
            playerList = client.target(BASE_URI).path("/players/position/power forward/name/Jack Patterson")
                    .request(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<PlayerDetails>>() {});
            printDetailsList(playerList);
            System.out.println();

            System.out.println("List all players in the city of Truckee:");
            playerList = client.target(BASE_URI).path("/players/city/Truckee")
                    .request(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<PlayerDetails>>() {});
            printDetailsList(playerList);
            System.out.println();

            System.out.println("List all soccer players:");
            playerList = client.target(BASE_URI).path("/players/sport/Soccer")
                    .request(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<PlayerDetails>>() {});
            printDetailsList(playerList);
            System.out.println();

            System.out.println("List all players in league L1:");
            playerList = client.target(BASE_URI).path("/players/league/L1")
                    .request(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<PlayerDetails>>() {});
            printDetailsList(playerList);
            System.out.println();

            System.out.println("List all players making a higher salary than Ian Carlyle:");
            playerList = client.target(BASE_URI).path("/players/salary/higher/Ian Carlyle")
                    .request(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<PlayerDetails>>() {});
            printDetailsList(playerList);
            System.out.println();

            System.out.println("List all players with a salary between 500 and 800:");
            playerList = client.target(BASE_URI).path("/players/salary/range")
                    .queryParam("low", 500.00).queryParam("high", 800.00)
                    .request(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<PlayerDetails>>() {});
            printDetailsList(playerList);
            System.out.println();

            System.out.println("List all players of team T5:");
            playerList = client.target(BASE_URI).path("/team/T5/players")
                    .request(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<PlayerDetails>>() {});
            printDetailsList(playerList);
            System.out.println();

            System.out.println("List all the leagues of player P28:");
            List<LeagueDetails> leagueList = client.target(BASE_URI).path("/player/P28/leagues")
                    .request(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<LeagueDetails>>() {});
            printDetailsList(leagueList);
            System.out.println();

            System.out.println("List all the sports of player P28:");
            List<String> sportList = client.target(BASE_URI).path("/player/P28/sports")
                    .request(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<String>>() {});
            printDetailsList(sportList);
            System.out.println();
        } catch (Exception ex) {
            System.err.println("Caught an exception: " + ex.getMessage());
        }
    }

    private void removeInfo(Client client) {
        try {
            System.out.println("Removing team T6.");
            client.target(BASE_URI).path("/team/T6").request().delete();
            System.out.println();

            System.out.println("Removing player P24");
            client.target(BASE_URI).path("/player/P24").request().delete();
            System.out.println();
        } catch (Exception ex) {
            System.err.println("Caught an exception: " + ex.getMessage());
        }
    }

    private static void printDetailsList(List<?> list) {
        for (Object details : list) {
            System.out.println(details.toString());
        }
        System.out.println();
    }
}

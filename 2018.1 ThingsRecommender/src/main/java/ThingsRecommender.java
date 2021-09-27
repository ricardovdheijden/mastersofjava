import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ThingsRecommender implements Recommender {
    private final Collection<Rating> dataset;

    public ThingsRecommender(Collection<Rating> dataset) {
        this.dataset = dataset;
    }

    /**
     * Calculate the euclidian distance between p1 and p2
     * @param p1 the first person
     * @param p2 the second person
     */
    public float euclidianDistance(String p1, String p2) {
        List<String> sharedThings = getThings(p1).stream()
                .filter(getThings(p2)::contains)
                .collect(Collectors.toList());

        List<Float> diffSquared = sharedThings.stream().map(thing -> {
            Float score1 = getScore(p1, thing);
            Float score2 = getScore(p2, thing);

            return (score1 - score2) * (score1 - score2);
        }).collect(Collectors.toList());

        return (float) Math.sqrt(diffSquared.stream().reduce(0f, Float::sum));
    }

    /**
     * Find the person with the most similar taste for a given person.
     * @param person the person to find a recommended person for
     */
    @Override
    public String closestPerson(String person) {
        Collection<String> allOtherPersons = getPersons().stream()
                .filter(p -> !p.equals(person))
                .collect(Collectors.toSet());

        Optional<String> closestPerson = allOtherPersons.stream().reduce((prev, curr) -> {
            float prevScore = euclidianDistance(prev, person);
            float currScore = euclidianDistance(curr, person);

            return currScore < prevScore ? curr : prev;
        });

        return closestPerson.orElse(null);
    }

    /**
     * Make a final recommendation for that person.
     * @param person the person to recommend a thing for
     * @return a thing that this person will probably like, based on the provided dataset.
     */
    @Override
    public String recommend(String person) {
        String closestPerson = closestPerson(person);
        Collection<Rating> closestPersonRatings = getRatings(closestPerson);

        Optional<Rating> recommendations = closestPersonRatings.stream()
                .filter(rating -> !getThings(person).contains(rating.getThing()))
                .reduce((prev, curr) -> prev.getScore() > curr.getScore() ? prev : curr);

        return recommendations.map(Rating::getThing).orElse(null);
    }

    private Collection<Rating> getRatings(String person) {
        return dataset.stream().filter(r -> r.getPerson().equals(person)).collect(Collectors.toSet());
    }

    private Collection<String> getPersons() {
        return dataset.stream().map(r -> r.getPerson()).collect(Collectors.toSet());
    }

    private Collection<String> getThings(String person) {
        return dataset.stream().filter(r -> r.getPerson().equals(person)).map(r -> r.getThing()).collect(Collectors.toSet());
    }

    private Float getScore(String person, String thing) {
        return dataset.stream().filter(r -> r.getPerson().equals(person) && r.getThing().equals(thing)).map(r -> r.getScore()).findFirst().orElse(null);
    }

}

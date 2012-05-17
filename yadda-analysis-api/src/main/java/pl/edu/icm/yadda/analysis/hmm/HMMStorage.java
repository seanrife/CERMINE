package pl.edu.icm.yadda.analysis.hmm;

import java.io.IOException;
import pl.edu.icm.yadda.analysis.hmm.probability.HMMEmissionProbability;
import pl.edu.icm.yadda.analysis.hmm.probability.HMMInitialProbability;
import pl.edu.icm.yadda.analysis.hmm.probability.HMMProbabilityInfo;
import pl.edu.icm.yadda.analysis.hmm.probability.HMMTransitionProbability;

/**
 * Hidden Markov Models storage interface. The interface takes care of storing 
 * and fetching HMM probability information.
 *
 * @author Dominika Tkaczyk (d.tkaczyk@icm.edu.pl)
 */
public interface HMMStorage {

    /**
     * Stores HMM initial probability.
     *
     * @param <S> A type of labels.
     * @param <T> A type of observations.
     * @param hmmId HMM id.
     * @param probability Probability object to store.
     * @throws IOException
     */
    <S,T> void storeInitialProbability(String hmmId, HMMInitialProbability<S> probability) throws IOException;

    /**
     * Stores HMM transition probability.
     *
     * @param <S> A type of labels.
     * @param <T> A type of observations.
     * @param hmmId HMM id.
     * @param probability Probability object to store.
     * @throws IOException
     */
    <S,T> void storeTransitionProbability(String hmmId, HMMTransitionProbability<S> probability) throws IOException;

    /**
     * Stores HMM emission probability.
     *
     * @param <S> A type of labels.
     * @param <T> A type of observations.
     * @param hmmId HMM id.
     * @param probability Probability object to store.
     * @throws IOException
     */
    <S,T> void storeEmissionProbability(String hmmId, HMMEmissionProbability<S,T> probability) throws IOException;

    /**
     * Fetches stored probability information object.
     *
     * @param <S> A type of labels.
     * @param <T> A type of observations.
     * @param hmmId HMM id.
     * @return Stored probability object.
     */
    <S,T> HMMProbabilityInfo<S,T> getProbabilityInfo(String hmmId) throws IOException;
}
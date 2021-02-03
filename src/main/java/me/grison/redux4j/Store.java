package me.grison.redux4j;

import com.google.gson.Gson;
import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Observable;
import java.util.Observer;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

import static io.vavr.control.Option.none;
import static io.vavr.control.Option.some;

/**
 * This is a Store implementation.
 *
 * @author Alexandre Grison (a.grison@gmail.com)
 */
public class Store<State, Action>
        extends Observable
        implements ReduxStore<State, Action>, ConsumerSubscription<State>, ObserverSubscription {

    static final Logger MIDDLEWARE_LOGGER = LoggerFactory.getLogger("redux4j.middleware");
    static final Logger STATE_LOGGER = LoggerFactory.getLogger("redux4j.state");
    static final Logger DISPATCHER_LOGGER = LoggerFactory.getLogger("redux4j.dispatcher");
    static final Logger REDUCER_LOGGER = LoggerFactory.getLogger("redux4j.reducer");
    static final Logger NOTIFIER_LOGGER = LoggerFactory.getLogger("redux4j.notifier");
    static final Logger SUBSCRIBING_LOGGER = LoggerFactory.getLogger("redux4j.subscribing");

    State currentState;
    io.vavr.collection.Map<UUID, Consumer<State>> consumers = io.vavr.collection.HashMap.empty();
    final Reducer<Action, State> reducer;
    final ReadWriteLock lock = new ReentrantReadWriteLock();
    final Option<Consumer<Action>> middlewareStack;

    final Gson gson = new Gson();

    /**
     * Creates a store with an initial state, and a reducer function.
     *
     * @param initialState the initial state
     * @param reducer      the reducer
     * @param middlewares  the middlewares
     */
    public static <State, Action> Store<State, Action> create(State initialState, Reducer<Action, State> reducer,
                                                              Middleware<State, Action>... middlewares) {
        return new Store<>(initialState, reducer, middlewares);
    }

    /**
     * A store takes an initial state, and a reducer function.
     *
     * @param initialState the initial state
     * @param reducer      the reducer
     * @param middlewares  the middlewares
     */
    private Store(State initialState, Reducer<Action, State> reducer, Middleware<State, Action>... middlewares) {
        currentState = initialState;
        this.reducer = reducer;

        STATE_LOGGER.debug("Creating Store with initial state: " + json(currentState));

        if (middlewares == null || middlewares.length == 0) {
            MIDDLEWARE_LOGGER.debug("Created store with no middleware.");

            middlewareStack = none();
        } else {
            MIDDLEWARE_LOGGER.debug("Created store with {} middlewares.", middlewares.length);

            middlewareStack = some((action) -> List.of(middlewares)
                    .fold((s1, a1, m) -> internalDispatch(action),
                            (m1, m2) -> (c, d, e) -> m2.accept(getState(), action, m1)
                    )
                    .accept(getState(), action, null));
        }
    }

    @Override
    public State getState() {
        lock.readLock().lock();
        if (STATE_LOGGER.isDebugEnabled()) {
            STATE_LOGGER.debug("Getting state: " + json(currentState));
        }

        return Try.of(() -> currentState)
                .andFinally(() -> lock.readLock().unlock())
                .get();
    }

    @Override
    public void dispatch(Action action) {
        middlewareStack
                .onEmpty(() -> internalDispatch(action))
                .peek(m -> m.accept(action));
    }

    private void internalDispatch(Action action) {
        if (DISPATCHER_LOGGER.isDebugEnabled()) {
            DISPATCHER_LOGGER.debug("Dispatching: " + json(action));
        }

        reduce(action);
        notifySubscribers();
    }

    private void reduce(Action action) {
        if (REDUCER_LOGGER.isDebugEnabled()) {
            REDUCER_LOGGER.debug("Will reduce: " + json(currentState));
        }

        lock.writeLock().lock();
        currentState = Try.of(() -> reducer.apply(action, currentState))
                .andFinally(() -> lock.writeLock().unlock())
                .get();

        if (REDUCER_LOGGER.isDebugEnabled()) {
            REDUCER_LOGGER.debug("Reduced state to: " + json(currentState));
        }
    }

    private void notifySubscribers() {
        NOTIFIER_LOGGER.debug("Notifying {} subscribers.", consumers.size() + countObservers());

        consumers.forEach(e -> e._2.accept(currentState));
        notifyObservers(currentState);
        setChanged();
    }

    public UUID subscribe(Consumer<State> subscriber) {
        var uuid = UUID.randomUUID();
        consumers = consumers.put(uuid, subscriber);

        SUBSCRIBING_LOGGER.debug("Subscribing {}. ID: {}.", subscriber, uuid);

        return uuid;
    }

    public void unsubscribe(Consumer<State> subscriber) {
        SUBSCRIBING_LOGGER.debug("Un-subscribing {}.", subscriber);
        consumers = consumers.filter(e -> !e._2.equals(subscriber));
    }

    public void unsubscribe(UUID subscriberId) {
        SUBSCRIBING_LOGGER.debug("Un-subscribing {}.", subscriberId);
        consumers.remove(subscriberId);
    }

    @Override
    public void subscribe(Observer observer) {
        SUBSCRIBING_LOGGER.debug("Subscribing {}.", observer);
        addObserver(observer);
    }

    @Override
    public void unsubscribe(Observer observer) {
        SUBSCRIBING_LOGGER.debug("Un-subscribing {}.", observer);
        deleteObserver(observer);
    }

    private String json(Object element) {
        return gson.toJson(element);
    }
}

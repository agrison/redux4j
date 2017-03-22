package me.grison.redux4j;

import com.google.gson.Gson;
import javaslang.collection.List;
import org.slf4j.*;

import java.util.*;
import java.util.concurrent.locks.*;
import java.util.function.Consumer;

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
	final Reducer<Action, State> reducer;
	final Map<UUID, Consumer<State>> consumers = new HashMap<>();
	final ReadWriteLock lock = new ReentrantReadWriteLock();
	final Consumer<Action> middlewareStack;

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
		this.currentState = initialState;
		this.reducer = reducer;

		if (STATE_LOGGER.isDebugEnabled()) {
			STATE_LOGGER.debug("Creating Store with initial state: " + json(this.currentState));
		}

		if (middlewares == null || middlewares.length == 0) {
			if (MIDDLEWARE_LOGGER.isDebugEnabled()) {
				MIDDLEWARE_LOGGER.debug("Created store with no middleware.");
			}

			middlewareStack = null;
		} else {
			if (MIDDLEWARE_LOGGER.isDebugEnabled()) {
				MIDDLEWARE_LOGGER.debug("Created store with {} middlewares.", middlewares.length);
			}

			middlewareStack = (action) -> List.of(middlewares)
												  .fold((s1, a1, m) -> internalDispatch(action),
															   (m1, m2) -> (c, d, e) -> m2.accept(this, action, m1)
												  )
												  .accept(this, action, null);
		}
	}

	@Override
	public State getState() {
		lock.readLock().lock();
		try {
			if (STATE_LOGGER.isDebugEnabled()) {
				STATE_LOGGER.debug("Getting state: " + json(currentState));
			}

			return currentState;
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public void dispatch(Action action) {
		if (middlewareStack == null) {
			this.internalDispatch(action);
		} else {
			middlewareStack.accept(action);
		}
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
			REDUCER_LOGGER.debug("Will reduce: " + json(this.currentState));
		}

		lock.writeLock().lock();
		try {
			this.currentState = reducer.apply(action, this.currentState);
		} finally {
			lock.writeLock().unlock();
		}

		if (REDUCER_LOGGER.isDebugEnabled()) {
			REDUCER_LOGGER.debug("Reduced state to: " + json(this.currentState));
		}
	}

	private void notifySubscribers() {
		if (NOTIFIER_LOGGER.isDebugEnabled()) {
			NOTIFIER_LOGGER.debug("Notifying {} subscribers.", this.consumers.size() + countObservers());
		}

		consumers.values().parallelStream().forEach(e -> e.accept(currentState));
		notifyObservers(currentState);
		setChanged();
	}

	public UUID subscribe(Consumer<State> subscriber) {
		UUID uuid = UUID.randomUUID();
		this.consumers.put(uuid, subscriber);

		if (SUBSCRIBING_LOGGER.isDebugEnabled()) {
			SUBSCRIBING_LOGGER.debug("Subscribing {}. ID: {}.", subscriber, uuid);
		}

		return uuid;
	}

	public void unsubscribe(Consumer<State> subscriber) {
		if (SUBSCRIBING_LOGGER.isDebugEnabled()) {
			SUBSCRIBING_LOGGER.debug("Un-subscribing {}.", subscriber);
		}

		this.consumers.entrySet().stream()
				.filter(e -> e.getValue().equals(subscriber)).findFirst()
				.ifPresent(e -> this.consumers.remove(e.getKey()));
	}

	public void unsubscribe(UUID subscriberId) {
		if (SUBSCRIBING_LOGGER.isDebugEnabled()) {
			SUBSCRIBING_LOGGER.debug("Un-subscribing {}.", subscriberId);
		}

		this.consumers.remove(subscriberId);
	}

	@Override
	public void subscribe(Observer observer) {
		if (SUBSCRIBING_LOGGER.isDebugEnabled()) {
			SUBSCRIBING_LOGGER.debug("Subscribing {}.", observer);
		}

		addObserver(observer);
	}

	@Override
	public void unsubscribe(Observer observer) {
		if (SUBSCRIBING_LOGGER.isDebugEnabled()) {
			SUBSCRIBING_LOGGER.debug("Un-subscribing {}.", observer);
		}

		deleteObserver(observer);
	}

	private String json(Object element) {
		return gson.toJson(element);
	}
}

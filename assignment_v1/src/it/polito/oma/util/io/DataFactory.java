package it.polito.oma.util.io;

abstract public class DataFactory <T extends Data>{

	abstract public T newInstance();
}

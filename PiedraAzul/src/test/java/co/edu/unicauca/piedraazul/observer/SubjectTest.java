package co.edu.unicauca.piedraazul.observer;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class SubjectTest {

    @Test
    void attachDebeEvitarDuplicados() {
        Subject subject = new Subject();
        Observer observer = mock(Observer.class);

        subject.attach(observer);
        subject.attach(observer);
        subject.notifyObservers("hola");

        verify(observer, times(1)).update("hola");
    }

    @Test
    void detachDebeEliminarObserver() {
        Subject subject = new Subject();
        Observer observer = mock(Observer.class);

        subject.attach(observer);
        subject.detach(observer);
        subject.notifyObservers("mensaje");

        verifyNoInteractions(observer);
    }

    @Test
    void attachDebeIgnorarNull() {
        Subject subject = new Subject();

        subject.attach(null);
        subject.notifyObservers("mensaje");
    }
}

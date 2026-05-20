package co.edu.unicauca.piedraazul.model;

import co.edu.unicauca.piedraazul.model.enums.UserStatus;
import co.edu.unicauca.piedraazul.observer.Observer;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class UserTest {

    @Test
    void setStatusDebeNotificarObserver() {
        User user = new User();
        user.setUsername("julian");
        Observer observer = mock(Observer.class);
        user.attach(observer);

        user.setStatus(UserStatus.ACTIVE);

        verify(observer).update(contains("julian"));
        verify(observer).update(contains("ACTIVE"));
    }

    @Test
    void detachDebeEvitarNuevasNotificaciones() {
        User user = new User();
        user.setUsername("julian");
        Observer observer = mock(Observer.class);
        user.attach(observer);
        user.detach(observer);

        user.setStatus(UserStatus.INACTIVE);

        verifyNoInteractions(observer);
    }
}

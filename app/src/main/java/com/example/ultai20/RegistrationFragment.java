import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.ultai20.R;
import com.example.ultai.data.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

public class RegistrationFragment extends Fragment {

    private UserRepository userRepository;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userRepository = UserRepository.getInstance();
    }

    private void hideLoading() {
        // Implementation of hideLoading method
    }

    private void register(String email, String password) {
        // TODO: Получить username, gender, phone из полей ввода (пока передаем заглушки)
        String username = "Default Username"; // Заглушка
        String gender = ""; // Заглушка
        String phone = ""; // Заглушка

        // userRepository.register(email, password, new UserRepository.AuthCallback<FirebaseUser>() { // Старый вызов
        userRepository.register(username, email, password, gender, phone, new UserRepository.Callback<FirebaseUser>() { // Новый вызов с доп. параметрами и новым колбэком
            @Override
            public void onSuccess(FirebaseUser firebaseUser) {
                // Пользователь успешно зарегистрирован и вошел в систему
                Log.d("RegistrationFragment", "Registration successful, navigating to BasicQuestionnaireFragment.");
                hideLoading();
                // Переход к BasicQuestionnaireFragment после успешной регистрации
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                navController.navigate(R.id.action_registrationFragment_to_basicQuestionnaireFragment);
            }

            // @Override
            // public void onFailure(Exception e) { // Старый метод
            @Override
            public void onError(String message) { // Новый метод onError
                hideLoading(); // Убедимся, что индикатор скрыт при любой ошибке
                Log.e("RegistrationFragment", "Registration failed: " + message);
                // Проверяем сообщение об ошибке, чтобы определить причину
                // Firebase Auth обычно возвращает специфичные сообщения или коды ошибок
                if (message != null && message.contains("email address is already in use")) { // Пример проверки по тексту ошибки
                    // Конкретная ошибка: email уже используется
                    Toast.makeText(getContext(), "Этот email уже зарегистрирован.", Toast.LENGTH_LONG).show();
                } else {
                    // Другие ошибки Firebase Auth или базы данных
                    Toast.makeText(getContext(), "Ошибка регистрации: " + message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
} 
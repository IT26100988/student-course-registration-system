package com.example.studentregistration.registration.rule;

import com.example.studentregistration.accounts.service.StudentService;
import com.example.studentregistration.registration.model.RegistrationRequest;
import org.springframework.stereotype.Component;

@Component
public class ActiveStudentRule implements RegistrationRule {

    private final StudentService studentService; // M1 ගේ Service එක

    public ActiveStudentRule(StudentService studentService) {
        this.studentService = studentService;
    }

    @Override
    public boolean isAllowed(RegistrationRequest request) {
        try {
            // M1 ගේ Service එක හරහා Student ව හොයනවා (ID එකෙන්)
            var student = studentService.findById(request.getStudentId());
            return student != null;
        } catch (Exception e) {
            return false; // Student ව නැත්නම් හෝ Error එකක් ආවොත් අවසර නැහැ
        }
    }

    @Override
    public String getFailureMessage() {
        return "Student account does not exist or is inactive.";
    }
}
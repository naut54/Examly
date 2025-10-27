package com.octal.examly.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octal.examly.domain.model.Question
import com.octal.examly.domain.model.Subject
import com.octal.examly.domain.model.Test
import com.octal.examly.domain.model.TestConfiguration
import com.octal.examly.domain.model.TestMode
import com.octal.examly.domain.usecase.test.CreateTestUseCase
import com.octal.examly.presentation.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateTestViewModel @Inject constructor(
    private val createTestUseCase: CreateTestUseCase,
    private val subjectRepository: com.octal.examly.domain.repository.SubjectRepository
) : ViewModel() {

    companion object {
        private const val TAG = "CreateTestViewModel"
    }

    private val _creationState = MutableStateFlow<UiState<Long>>(UiState.Idle)
    val creationState: StateFlow<UiState<Long>> = _creationState.asStateFlow()

    private val _subjects = MutableStateFlow<List<Subject>>(emptyList())
    val subjects: StateFlow<List<Subject>> = _subjects.asStateFlow()

    private var testTitle: String = ""
    private var testDescription: String = ""
    private var selectedSubjectId: Long = 0L
    private var testMode: TestMode? = null
    private var selectedQuestions: List<Question> = emptyList()
    private var selectedSubjects: List<Subject> = emptyList()
    private var questionCount: Int = 0
    private var hasTimer: Boolean = false
    private var timeLimit: Int? = null
    private var hasPracticeMode: Boolean = false

    init {
        loadSubjects()
    }

    private fun loadSubjects() {
        viewModelScope.launch {
            subjectRepository.getAllSubjects().collect { subjectList ->
                _subjects.value = subjectList
            }
        }
    }

    fun setTestBasicInfo(title: String, description: String, subjectId: Long) {
        Log.d(TAG, "setTestBasicInfo: title='$title', description='$description', subjectId=$subjectId")
        this.testTitle = title
        this.testDescription = description
        this.selectedSubjectId = subjectId
    }

    fun setTitle(title: String) {
        Log.d(TAG, "setTitle: '$title'")
        this.testTitle = title
    }

    fun setDescription(description: String) {
        Log.d(TAG, "setDescription: '$description'")
        this.testDescription = description
    }

    fun setTestMode(mode: TestMode) {
        Log.d(TAG, "setTestMode: mode=$mode")
        testMode = mode
    }

    fun setSelectedQuestions(questions: List<Question>) {
        Log.d(TAG, "setSelectedQuestions: ${questions.size} questions")
        selectedQuestions = questions
    }

    fun setRandomConfiguration(
        subjects: List<Subject>,
        questionCount: Int
    ) {
        this.selectedSubjects = subjects
        this.questionCount = questionCount
    }

    fun setTimerConfiguration(hasTimer: Boolean, timeLimit: Int?) {
        this.hasTimer = hasTimer
        this.timeLimit = timeLimit
    }

    fun setPracticeMode(hasPracticeMode: Boolean) {
        this.hasPracticeMode = hasPracticeMode
    }

    fun createTest(createdBy: Long) {
        Log.d(TAG, "createTest called: createdBy=$createdBy")
        Log.d(TAG, "createTest: testTitle='$testTitle', testDescription='$testDescription'")
        Log.d(TAG, "createTest: testMode=$testMode, selectedQuestions.size=${selectedQuestions.size}, questionCount=$questionCount")
        Log.d(TAG, "createTest: selectedSubjectId=$selectedSubjectId")

        viewModelScope.launch {
            try {
                Log.d(TAG, "createTest: Setting state to Loading")
                _creationState.value = UiState.Loading

                val test = buildTestObject(testTitle, testDescription, createdBy)
                Log.d(TAG, "createTest: Built test object: mode=${test.mode}, subjectId=${test.subjectId}, config=${test.configuration}")

                val questionIds = if (testMode == TestMode.FIXED) {
                    selectedQuestions.map { it.id }
                } else {
                    null
                }
                Log.d(TAG, "createTest: questionIds=$questionIds")

                Log.d(TAG, "createTest: Calling createTestUseCase")
                val result = createTestUseCase(test, questionIds)
                result.fold(
                    onSuccess = { id ->
                        Log.d(TAG, "createTest: Success! Created test with ID=$id")
                        _creationState.value = UiState.Success(id)
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "createTest: Use case failed", exception)
                        _creationState.value = UiState.Error(
                            exception.message ?: "Failed to create test"
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "createTest: Exception caught", e)
                _creationState.value = UiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    private fun buildTestObject(title: String, description: String, createdBy: Long): Test {
        val configuration = TestConfiguration(
            hasPracticeMode = hasPracticeMode,
            hasTimer = hasTimer,
            timeLimit = timeLimit,
            numberOfQuestions = getQuestionCount(),
            questionSelection = when (testMode) {
                TestMode.FIXED -> com.octal.examly.domain.model.QuestionSelection.MANUAL
                TestMode.RANDOM -> com.octal.examly.domain.model.QuestionSelection.RANDOM
                else -> com.octal.examly.domain.model.QuestionSelection.MANUAL
            }
        )

        val subjectId = if (selectedSubjectId != 0L) {
            selectedSubjectId
        } else {
            when (testMode) {
                TestMode.FIXED -> selectedQuestions.firstOrNull()?.subjectId ?: 0L
                TestMode.RANDOM -> selectedSubjects.firstOrNull()?.id ?: 0L
                else -> 0L
            }
        }

        Log.d(TAG, "buildTestObject: Using subjectId=$subjectId")

        return Test(
            title = title,
            description = description,
            subjectId = subjectId,
            mode = testMode ?: TestMode.FIXED,
            configuration = configuration,
            createdBy = createdBy.toInt()
        )
    }

    fun getTestMode(): TestMode? = testMode

    fun getQuestionCount(): Int = selectedQuestions.size.takeIf { it > 0 } ?: questionCount

    fun hasTimer(): Boolean = hasTimer

    fun getTimeLimit(): Int = timeLimit ?: 0

    fun hasPracticeMode(): Boolean = hasPracticeMode

    fun getSelectedSubjects(): List<Subject> = selectedSubjects

    fun resetTestCreation() {
        Log.d(TAG, "resetTestCreation: Resetting all test data")
        testTitle = ""
        testDescription = ""
        selectedSubjectId = 0L
        testMode = null
        selectedQuestions = emptyList()
        selectedSubjects = emptyList()
        questionCount = 0
        hasTimer = false
        timeLimit = null
        hasPracticeMode = false
        _creationState.value = UiState.Idle
    }
}

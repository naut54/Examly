package com.octal.examly.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octal.examly.domain.model.Question
import com.octal.examly.domain.model.QuestionType
import com.octal.examly.domain.model.Subject
import com.octal.examly.domain.model.Answer
import com.octal.examly.domain.usecase.question.CreateQuestionUseCase
import com.octal.examly.domain.usecase.question.GetAllQuestionsUseCase
import com.octal.examly.domain.usecase.question.GetQuestionsBySubjectUseCase
import com.octal.examly.domain.usecase.subject.GetAllSubjectsUseCase
import com.octal.examly.presentation.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateQuestionViewModel @Inject constructor(
    private val createQuestionUseCase: CreateQuestionUseCase,
    private val getAllSubjectsUseCase: GetAllSubjectsUseCase,
    private val getQuestionsBySubjectUseCase: GetQuestionsBySubjectUseCase,
    private val getAllQuestionsUseCase: GetAllQuestionsUseCase
) : ViewModel() {

    private val _creationState = MutableStateFlow<UiState<Long>>(UiState.Idle)
    val creationState: StateFlow<UiState<Long>> = _creationState.asStateFlow()

    private val _subjectsState = MutableStateFlow<UiState<List<Subject>>>(UiState.Loading)
    val subjectsState: StateFlow<UiState<List<Subject>>> = _subjectsState.asStateFlow()

    private val _questionsState = MutableStateFlow<UiState<List<Question>>>(UiState.Loading)
    val questionsState: StateFlow<UiState<List<Question>>> = _questionsState.asStateFlow()

    private val _questionCounts = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val questionCounts: StateFlow<Map<Long, Int>> = _questionCounts.asStateFlow()

    private var allQuestions = listOf<Question>()

    fun loadSubjects() {
        viewModelScope.launch {
            try {
                _subjectsState.value = UiState.Loading

                getAllSubjectsUseCase().collect { subjects ->
                    _subjectsState.value = UiState.Success(subjects)
                }
            } catch (e: Exception) {
                _subjectsState.value = UiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun loadQuestionsBySubject(subjectId: Long) {
        viewModelScope.launch {
            try {
                _questionsState.value = UiState.Loading

                getQuestionsBySubjectUseCase(subjectId).collect { questions ->
                    _questionsState.value = UiState.Success(questions)
                }
            } catch (e: Exception) {
                _questionsState.value = UiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun loadAllQuestions() {
        viewModelScope.launch {
            try {
                _questionsState.value = UiState.Loading

                getAllQuestionsUseCase().collect { questions ->
                    allQuestions = questions
                    _questionsState.value = if (questions.isEmpty()) {
                        UiState.Empty
                    } else {
                        UiState.Success(questions)
                    }
                }
            } catch (e: Exception) {
                _questionsState.value = UiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun filterQuestions(query: String?) {
        viewModelScope.launch {
            try {
                val filtered = if (query.isNullOrBlank()) {
                    allQuestions
                } else {
                    allQuestions.filter { question ->
                        question.questionText.contains(query, ignoreCase = true)
                    }
                }

                _questionsState.value = if (filtered.isEmpty()) {
                    UiState.Empty
                } else {
                    UiState.Success(filtered)
                }
            } catch (e: Exception) {
                _questionsState.value = UiState.Error(e.message ?: "An error occurred")
            }
        }
    }

    fun loadQuestionCountBySubject(subjectId: Long) {
        viewModelScope.launch {
            try {
                getQuestionsBySubjectUseCase(subjectId).collect { questions ->
                    val currentCounts = _questionCounts.value.toMutableMap()
                    currentCounts[subjectId] = questions.size
                    _questionCounts.value = currentCounts
                }
            } catch (e: Exception) {
            }
        }
    }

    fun createQuestion(
        subjectId: Long,
        questionText: String,
        imageUri: String?,
        type: QuestionType,
        answers: List<Pair<String, Boolean>>,
        explanation: String?
    ) {
        viewModelScope.launch {
            try {
                _creationState.value = UiState.Loading

                if (questionText.isBlank()) {
                    _creationState.value = UiState.Error("Question text cannot be empty")
                    return@launch
                }

                if (answers.size < 2) {
                    _creationState.value = UiState.Error("At least 2 answers are required")
                    return@launch
                }

                if (answers.none { it.second }) {
                    _creationState.value = UiState.Error("At least one answer must be correct")
                    return@launch
                }

                val answersList = answers.map { (text, isCorrect) ->
                    Answer(answerText = text, isCorrect = isCorrect)
                }
                val question = Question(
                    subjectId = subjectId,
                    questionText = questionText,
                    imageUri = imageUri,
                    explanation = explanation,
                    type = type,
                    answers = answersList
                )
                val result = createQuestionUseCase(question)
                result.fold(
                    onSuccess = { id ->
                        _creationState.value = UiState.Success(id)
                    },
                    onFailure = { exception ->
                        _creationState.value = UiState.Error(
                            exception.message ?: "Failed to create question"
                        )
                    }
                )
            } catch (e: Exception) {
                _creationState.value = UiState.Error(e.message ?: "An error occurred")
            }
        }
    }
}

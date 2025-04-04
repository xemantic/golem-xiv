// DOM Elements
const themeToggle = document.getElementById('theme-toggle');
const menuToggle = document.getElementById('menu-toggle');
const sidebar = document.getElementById('sidebar');
const overlay = document.getElementById('overlay');
const newChatSidebar = document.getElementById('new-chat-sidebar');
const conversationList = document.getElementById('conversation-list');
const messagesContainer = document.getElementById('messages');
const promptInput = document.getElementById('prompt-input');
const sendButton = document.getElementById('send-button');
const micButton = document.getElementById('mic-button');
const stopRecordingButton = document.getElementById('stop-recording');
const micStatus = document.getElementById('mic-status');
const recordingTime = document.getElementById('recording-time');
const chatContainer = document.getElementById('chat-container');
const welcomeText = document.querySelector('.welcome-text');
const mainContainer = document.querySelector('.main-container');

// State
let isDarkMode = false;
let isRecording = false;
let mediaRecorder = null;
let audioChunks = [];
let recordingInterval = null;
let recordingSeconds = 0;
let isFirstMessage = true;
let conversations = [];
let currentConversationId = null;
let isSidebarOpen = false;

// Event Listeners
document.addEventListener('DOMContentLoaded', init);
themeToggle.addEventListener('click', toggleTheme);
menuToggle.addEventListener('click', toggleSidebar);
newChatSidebar.addEventListener('click', startNewChat);
overlay.addEventListener('click', toggleSidebar);
sendButton.addEventListener('click', handleSendMessage);
micButton.addEventListener('click', toggleSpeechRecording);
stopRecordingButton.addEventListener('click', stopRecording);

// Handle Enter key for sending message
promptInput.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        handleSendMessage();
    }
});

// Initialize the app
function init() {
    // Check for saved theme preference
    const savedTheme = localStorage.getItem('theme');
    if (savedTheme === 'dark') {
        toggleTheme();
    }

    // Ensure sidebar is closed by default
    applyDefaultSidebarState();

    // Set overflow on body to enable single scrollbar
    document.body.style.overflowY = 'auto';

    // Load conversations from local storage
    loadConversations();

    // Focus on input field
    promptInput.focus();

    // Resize textarea as content changes
    promptInput.addEventListener('input', () => {
        promptInput.style.height = 'auto';
        promptInput.style.height = Math.min(promptInput.scrollHeight, 150) + 'px';
    });

    // Listen for window resize events
    window.addEventListener('resize', handleWindowResize);
}

// Apply default sidebar state based on window size
function applyDefaultSidebarState() {
    isSidebarOpen = false;

    // Remove any existing classes first
    sidebar.classList.remove('sidebar-visible', 'sidebar-hidden-desktop', 'sidebar-hidden-mobile');
    overlay.classList.remove('overlay-visible');
    mainContainer.classList.remove('main-container-sidebar-open');

    // Apply the correct hidden class based on screen size
    if (window.innerWidth <= 768) {
        sidebar.classList.add('sidebar-hidden-mobile');
    } else {
        sidebar.classList.add('sidebar-hidden-desktop');
    }
}

// Handle window resize
function handleWindowResize() {
    // Update sidebar positioning based on screen size
    if (window.innerWidth <= 768) {
        // Mobile view
        sidebar.classList.remove('sidebar-hidden-desktop', 'sidebar-visible');

        if (!isSidebarOpen) {
            sidebar.classList.add('sidebar-hidden-mobile');
        } else {
            sidebar.classList.add('sidebar-visible');
        }
    } else {
        // Desktop view
        sidebar.classList.remove('sidebar-hidden-mobile', 'sidebar-visible');

        if (!isSidebarOpen) {
            sidebar.classList.add('sidebar-hidden-desktop');
        } else {
            sidebar.classList.add('sidebar-visible');
        }
    }

    // Close sidebar automatically when resizing to mobile view if it's open
    if (window.innerWidth <= 768 && isSidebarOpen) {
        toggleSidebar();
    }
}

// Toggle sidebar
function toggleSidebar() {
    isSidebarOpen = !isSidebarOpen;

    // First remove all positioning classes
    sidebar.classList.remove('sidebar-hidden-desktop', 'sidebar-hidden-mobile', 'sidebar-visible');

    // Add the appropriate class based on sidebar state and screen size
    if (isSidebarOpen) {
        sidebar.classList.add('sidebar-visible');
        overlay.classList.add('overlay-visible');

        // On desktop, also shift the main container
        if (window.innerWidth > 768) {
            mainContainer.classList.add('main-container-sidebar-open');
        }
    } else {
        // Apply the correct hidden class based on screen size
        if (window.innerWidth <= 768) {
            sidebar.classList.add('sidebar-hidden-mobile');
        } else {
            sidebar.classList.add('sidebar-hidden-desktop');
        }

        overlay.classList.remove('overlay-visible');
        mainContainer.classList.remove('main-container-sidebar-open');
    }

    // Ensure focus returns to input when sidebar closes
    if (!isSidebarOpen) {
        promptInput.focus();
    }
}

// Toggle between light and dark theme
function toggleTheme() {
    isDarkMode = !isDarkMode;
    document.body.classList.toggle('dark-theme', isDarkMode);

    // Update the theme toggle button contents
    themeToggle.querySelector('i').className = isDarkMode ? 'fas fa-sun' : 'fas fa-moon';
    themeToggle.querySelector('span').textContent = isDarkMode ? 'Light Mode' : 'Dark Mode';

    localStorage.setItem('theme', isDarkMode ? 'dark' : 'light');
}

// Load conversations from local storage
function loadConversations() {
    const savedConversations = localStorage.getItem('golem-conversations');
    if (savedConversations) {
        conversations = JSON.parse(savedConversations);
        renderConversationList();
    }
}

// Save conversations to local storage
function saveConversations() {
    localStorage.setItem('golem-conversations', JSON.stringify(conversations));
}

// Render conversation list in sidebar
function renderConversationList() {
    // Clear the list first
    conversationList.innerHTML = '';

    if (conversations.length === 0) {
        conversationList.innerHTML = '<li class="no-conversations">No conversations yet</li>';
        return;
    }

    // Sort conversations by date (newest first)
    const sortedConversations = [...conversations].sort((a, b) => b.lastUpdated - a.lastUpdated);

    // Add each conversation to the list
    sortedConversations.forEach(convo => {
        const li = document.createElement('li');
        li.dataset.id = convo.id;
        if (convo.id === currentConversationId) {
            li.classList.add('active');
        }

        const iconClass = convo.id === currentConversationId ? 'fa-comment-dots' : 'fa-comment';

        li.innerHTML = `
            <i class="fas ${iconClass}"></i>
            <span class="conversation-title">${convo.title}</span>
            <button class="delete-conversation" title="Delete conversation">
                <i class="fas fa-trash"></i>
            </button>
        `;

        // Add click event to load this conversation
        li.addEventListener('click', (e) => {
            if (!e.target.closest('.delete-conversation')) {
                loadConversation(convo.id);
            }
        });

        // Add delete button event
        const deleteBtn = li.querySelector('.delete-conversation');
        deleteBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            deleteConversation(convo.id);
        });

        conversationList.appendChild(li);
    });
}

// Create a new conversation
function createConversation(firstMessage) {
    const id = Date.now().toString();
    const newConversation = {
        id,
        title: firstMessage.length > 30 ? firstMessage.substring(0, 30) + '...' : firstMessage,
        messages: [],
        created: Date.now(),
        lastUpdated: Date.now()
    };

    conversations.push(newConversation);
    currentConversationId = id;
    saveConversations();
    renderConversationList();
    return newConversation;
}

// Load a specific conversation
function loadConversation(id) {
    const conversation = conversations.find(c => c.id === id);
    if (!conversation) return;

    currentConversationId = id;
    switchToConversationMode();

    // Clear current messages
    messagesContainer.innerHTML = '';

    // Add all messages from this conversation
    conversation.messages.forEach(msg => {
        if (msg.role === 'user') {
            addMessage(msg.content, 'user', false);
        } else {
            const container = addMessage(msg.content, 'ai', false);
            // Don't animate old messages
            container.style.animation = 'none';
        }
    });

    renderConversationList();

    // Close sidebar after selecting a conversation
    if (isSidebarOpen) {
        toggleSidebar();
    }

    // Scroll to bottom of messages
    scrollToBottom();
}

// Helper function to scroll to the bottom of messages
function scrollToBottom() {
    window.scrollTo({
        top: document.body.scrollHeight,
        behavior: 'smooth'
    });
}

// Delete a conversation
function deleteConversation(id) {
    const index = conversations.findIndex(c => c.id === id);
    if (index !== -1) {
        conversations.splice(index, 1);
        saveConversations();

        // If we deleted the current conversation, start a new one
        if (currentConversationId === id) {
            startNewChat();
        } else {
            renderConversationList();
        }
    }
}

// Start a new chat
function startNewChat() {
    messagesContainer.innerHTML = '';
    promptInput.value = '';
    promptInput.style.height = 'auto';
    promptInput.focus();

    // Return to centered mode
    isFirstMessage = true;
    currentConversationId = null;

    // Switch to centered mode using class
    chatContainer.classList.remove('chat-conversation-mode');
    chatContainer.classList.add('chat-centered-mode');

    // Show welcome text
    welcomeText.classList.remove('welcome-text-hidden');

    renderConversationList();

    // Close sidebar after starting a new chat
    if (isSidebarOpen) {
        toggleSidebar();
    }
}

// Switch from centered mode to conversation mode
function switchToConversationMode() {
    if (isFirstMessage) {
        isFirstMessage = false;

        // Switch to conversation mode using classes
        chatContainer.classList.remove('chat-centered-mode');
        chatContainer.classList.add('chat-conversation-mode');

        // Hide welcome text
        welcomeText.classList.add('welcome-text-hidden');
    }
}

// Handle sending a message
function handleSendMessage() {
    const message = promptInput.value.trim();
    if (!message) return;

    // Switch to conversation mode if this is the first message
    switchToConversationMode();

    // Create a new conversation if this is the first message of a new chat
    if (!currentConversationId) {
        const newConversation = createConversation(message);
        currentConversationId = newConversation.id;
    }

    // Add user message to chat
    addMessage(message, 'user');

    // Save message to current conversation
    const conversation = conversations.find(c => c.id === currentConversationId);
    if (conversation) {
        conversation.messages.push({
            role: 'user',
            content: message,
            timestamp: Date.now()
        });
        conversation.lastUpdated = Date.now();
        saveConversations();
        renderConversationList();
    }

    // Clear input field
    promptInput.value = '';
    promptInput.style.height = 'auto';
    promptInput.focus();

    // Request AI response
    requestAIResponse(message);
}

// Add a message to the chat
function addMessage(content, sender, save = true) {
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${sender}-message`;

    const header = document.createElement('div');
    header.className = 'message-header';

    if (sender === 'user') {
        header.innerHTML = '<i class="fas fa-user"></i> You';
    } else {
        header.innerHTML = '<i class="fas fa-robot"></i> Golem XIV';
    }

    const contentDiv = document.createElement('div');
    contentDiv.className = 'message-content';
    contentDiv.textContent = content;

    messageDiv.appendChild(header);
    messageDiv.appendChild(contentDiv);
    messagesContainer.appendChild(messageDiv);

    // Scroll to the new message using body scrolling
    scrollToBottom();

    return contentDiv; // Return for streaming purposes
}

// Request AI response from the API
async function requestAIResponse(prompt) {
    // Placeholder for the future API connection
    const responseContainer = addMessage('', 'ai', false);

    // Simulate streaming response for demo purposes
    const demoResponse = "I am Golem XIV, your AI assistant. I'm here to help you with information, creative content, problem-solving, and thoughtful conversations. How can I assist you today?";

    // Simulate streaming by adding characters one by one
    let i = 0;
    const fullResponse = []; // To build the complete response

    const streamInterval = setInterval(() => {
        if (i < demoResponse.length) {
            const char = document.createElement('span');
            char.className = 'character';
            char.textContent = demoResponse[i];
            responseContainer.appendChild(char);
            fullResponse.push(demoResponse[i]);
            i++;

            // Scroll to bottom as new content appears
            if (i % 5 === 0) { // Only scroll every few characters for performance
                scrollToBottom();
            }
        } else {
            clearInterval(streamInterval);

            // Final scroll to ensure we're at the bottom
            scrollToBottom();

            // Save the AI response to the conversation
            const conversation = conversations.find(c => c.id === currentConversationId);
            if (conversation) {
                conversation.messages.push({
                    role: 'assistant',
                    content: fullResponse.join(''),
                    timestamp: Date.now()
                });
                conversation.lastUpdated = Date.now();
                saveConversations();
            }
        }
    }, 30);

    /*
    // This will be the actual implementation in the future
    try {
        const response = await fetch('YOUR_API_ENDPOINT', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ prompt })
        });

        if (!response.ok) throw new Error('API request failed');

        // Handle streaming response
        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        const fullResponse = [];

        while (true) {
            const { done, value } = await reader.read();
            if (done) break;

            const text = decoder.decode(value, { stream: true });
            fullResponse.push(text);
            // Append text to the message
            responseContainer.textContent += text;
            scrollToBottom();
        }

        // Save the AI response to the conversation
        const conversation = conversations.find(c => c.id === currentConversationId);
        if (conversation) {
            conversation.messages.push({
                role: 'assistant',
                content: fullResponse.join(''),
                timestamp: Date.now()
            });
            conversation.lastUpdated = Date.now();
            saveConversations();
        }
    } catch (error) {
        console.error('Error:', error);
        responseContainer.textContent = 'Sorry, there was an error processing your request.';

        // Save the error message too
        const conversation = conversations.find(c => c.id === currentConversationId);
        if (conversation) {
            conversation.messages.push({
                role: 'assistant',
                content: 'Sorry, there was an error processing your request.',
                timestamp: Date.now(),
                error: true
            });
            conversation.lastUpdated = Date.now();
            saveConversations();
        }
    }
    */
}

// Toggle speech recording
function toggleSpeechRecording() {
    if (isRecording) {
        stopRecording();
    } else {
        startRecording();
    }
}

// Start recording audio
async function startRecording() {
    try {
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true });

        mediaRecorder = new MediaRecorder(stream);
        audioChunks = [];

        mediaRecorder.addEventListener('dataavailable', event => {
            audioChunks.push(event.data);
        });

        mediaRecorder.addEventListener('stop', processRecording);

        mediaRecorder.start();
        isRecording = true;

        // Use class for mic button active state
        micButton.classList.add('mic-button-active');
        micStatus.classList.remove('hidden');

        // Start timer
        recordingSeconds = 0;
        updateRecordingTime();
        recordingInterval = setInterval(updateRecordingTime, 1000);

    } catch (error) {
        console.error('Error accessing microphone:', error);
        alert('Could not access the microphone. Please check your permissions.');
    }
}

// Stop recording audio
function stopRecording() {
    if (!mediaRecorder) return;

    mediaRecorder.stop();
    mediaRecorder.stream.getTracks().forEach(track => track.stop());
    isRecording = false;

    // Remove active class from mic button
    micButton.classList.remove('mic-button-active');
    micStatus.classList.add('hidden');
    
    clearInterval(recordingInterval);
}

// Process the recorded audio
function processRecording() {
    const audioBlob = new Blob(audioChunks, { type: 'audio/webm' });
    
    // Here you would normally send this blob to your speech-to-text API
    // For demo purposes, we'll just show a message that would represent the transcribed text
    
    promptInput.value = "[Transcribed speech would appear here]";
    promptInput.style.height = 'auto';
    promptInput.style.height = Math.min(promptInput.scrollHeight, 150) + 'px';
    promptInput.focus();
    
    /* 
    // This will be the actual implementation in the future
    const formData = new FormData();
    formData.append('audio', audioBlob);
    
    fetch('YOUR_SPEECH_TO_TEXT_API', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        promptInput.value = data.transcript;
        promptInput.style.height = 'auto';
        promptInput.style.height = Math.min(promptInput.scrollHeight, 150) + 'px';
        promptInput.focus();
        
        // If we got a transcript and it's not empty, automatically send it
        if (data.transcript && data.transcript.trim()) {
            handleSendMessage();
        }
    })
    .catch(error => {
        console.error('Speech to text error:', error);
        alert('Failed to process speech. Please try again.');
    });
    */
}

// Update the recording time display
function updateRecordingTime() {
    recordingSeconds++;
    const minutes = Math.floor(recordingSeconds / 60);
    const seconds = recordingSeconds % 60;
    recordingTime.textContent = `${minutes}:${seconds.toString().padStart(2, '0')}`;
}

// Adjust height of the textarea based on content
function adjustTextareaHeight() {
    promptInput.style.height = 'auto';
    promptInput.style.height = Math.min(promptInput.scrollHeight, 150) + 'px';
}
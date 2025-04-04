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
    
    // Explicitly ensure sidebar is closed by default
    sidebar.classList.remove('active');
    overlay.classList.remove('active');
    document.body.classList.remove('sidebar-active');
    isSidebarOpen = false;
    
    // Force sidebar to be positioned off-screen initially
    sidebar.style.left = '-' + getComputedStyle(document.documentElement).getPropertyValue('--sidebar-width');
    
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

// Handle window resize
function handleWindowResize() {
    // Close sidebar automatically when resizing to mobile view if it's open
    if (window.innerWidth <= 768 && isSidebarOpen) {
        toggleSidebar();
    }
}

// Toggle sidebar
function toggleSidebar() {
    isSidebarOpen = !isSidebarOpen;
    sidebar.classList.toggle('active', isSidebarOpen);
    overlay.classList.toggle('active', isSidebarOpen);
    document.body.classList.toggle('sidebar-active', isSidebarOpen);
    
    // Explicitly set the left property when toggling
    if (isSidebarOpen) {
        sidebar.style.left = '0';
    } else {
        sidebar.style.left = '-' + getComputedStyle(document.documentElement).getPropertyValue('--sidebar-width');
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
    themeToggle.innerHTML = isDarkMode ? '<i class="fas fa-sun"></i>' : '<i class="fas fa-moon"></i>';
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
    chatContainer.classList.add('centered-mode');
    welcomeText.classList.remove('hidden');
    
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
        chatContainer.classList.remove('centered-mode');
        welcomeText.classList.add('hidden');
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
    
    // Scroll to the new message
    messageDiv.scrollIntoView({ behavior: 'smooth' });
    
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
            char.scrollIntoView({ behavior: 'smooth' });
        } else {
            clearInterval(streamInterval);
            
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
            responseContainer.scrollIntoView({ behavior: 'smooth' });
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
        micButton.classList.add('active');
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
    micButton.classList.remove('active');
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
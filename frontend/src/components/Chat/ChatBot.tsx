import { useState, useRef, useEffect } from 'react';
import { chatApi } from '../../api';
import styles from './ChatBot.module.css';

interface Message {
  role: 'user' | 'assistant';
  content: string;
}

export default function ChatBot() {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState<Message[]>([
    { role: 'assistant', content: '안녕하세요! 운동 관련 질문이 있으시면 뭐든 물어보세요 💪' }
  ]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const bottomRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSend = async () => {
    if (!input.trim() || isLoading) return;

    const userMessage = input.trim();
    setInput('');
    setMessages(prev => [...prev, { role: 'user', content: userMessage }]);
    setIsLoading(true);

    try {
      const response = await chatApi.sendMessage(userMessage);
      setMessages(prev => [...prev, { role: 'assistant', content: response.data.reply }]);
    } catch {
      setMessages(prev => [...prev, { role: 'assistant', content: '오류가 발생했습니다. 다시 시도해주세요.' }]);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <>
      {/* 챗봇 열기 버튼 */}
      <button className={styles.fab} onClick={() => setIsOpen(prev => !prev)}>
        {isOpen ? '✕' : '💬'}
      </button>

      {/* 챗봇 창 */}
      {isOpen && (
        <div className={styles.chatWindow}>
          <div className={styles.header}>
            <span>🏋️ 운동 트레이너 AI</span>
          </div>

          <div className={styles.messages}>
            {messages.map((msg, i) => (
              <div key={i} className={msg.role === 'user' ? styles.userMsg : styles.assistantMsg}>
                {msg.content}
              </div>
            ))}
            {isLoading && (
              <div className={styles.assistantMsg}>답변 작성 중...</div>
            )}
            <div ref={bottomRef} />
          </div>

          <div className={styles.inputArea}>
            <input
              className={styles.input}
              value={input}
              onChange={e => setInput(e.target.value)}
              onKeyDown={e => e.key === 'Enter' && handleSend()}
              placeholder="운동 관련 질문을 입력하세요..."
            />
            <button className={styles.sendBtn} onClick={handleSend} disabled={isLoading}>
              전송
            </button>
          </div>
        </div>
      )}
    </>
  );
}
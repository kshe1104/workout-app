declare module '*.module.css' {
  const classes: { [key: string]: string };
  export default classes;
}

// Vite 환경변수 타입 선언 추가
interface ImportMeta {
  readonly env: {
    readonly VITE_API_BASE_URL?: string;
    [key: string]: string | undefined;
  };
}
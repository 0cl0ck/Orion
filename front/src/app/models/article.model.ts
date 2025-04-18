export interface Article {
  id: number;
  title: string;
  content: string;
  createdAt: string;
  authorId: number;
  authorUsername: string;
  themeId: number;
  themeName: string;
  commentCount: number;
}

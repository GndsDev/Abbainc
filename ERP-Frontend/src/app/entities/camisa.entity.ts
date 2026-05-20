export interface Camisa {
  id?: number;
  sku: string;
  modelo: string;
  cor: string;
  tamanho: string;
  quantidadeEmEstoque: number;
  preco: number;
  imagemUrl?: string;
}

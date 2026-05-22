export interface VariacaoProduto {
  id?: number;
  tamanho: string;
  sku: string;
  quantidadeEmEstoque: number;
}

export interface Produto {
  id?: number;
  modelo: string;
  cor: string;
  preco: number;
  imagemUrl?: string;
  variacoes: VariacaoProduto[];
}

export interface ItemEstoque {
  id?: number;
  produtoId?: number;
  modelo: string;
  cor: string;
  tamanho: string;
  sku: string;
  quantidadeEmEstoque: number;
  preco: number;
  imagemUrl?: string;
}

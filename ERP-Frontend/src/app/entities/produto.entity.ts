export interface VariacaoProduto {
  id?: number;
  tamanho: string;
  sku: string;
  quantidadeEmEstoque: number;
}

export type TipoImagemProduto = 'CAPA' | 'COSTAS' | 'FRENTE' | 'DETALHE';

export interface ProdutoImagem {
  id: number;
  url: string;
  tipo: TipoImagemProduto;
  ordem: number;
  altText?: string;
  principal: boolean;
}

export interface Produto {
  id?: number;
  modelo: string;
  cor: string;
  preco: number;
  imagemUrl?: string;
  imagens?: ProdutoImagem[];
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

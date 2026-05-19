export interface ItemPedidoDTO {
  camisa: { id: number };
  quantidade: number;
}

export interface PedidoDTO {
  cliente: { id: number };
  formaPagamento: string;
  itens: ItemPedidoDTO[];
}

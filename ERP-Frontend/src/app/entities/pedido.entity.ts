import { Produto, VariacaoProduto } from './produto.entity';
import { Cliente } from './cliente.entity';

export interface PedidoDTO {
  cliente: { id: number };
  formaPagamento: string;
  itens: { variacaoProduto: { id: number }, quantidade: number }[];
}

export interface Pedido {
  id: number;
  dataPedido: string;
  cliente: Cliente;
  formaPagamento: string;
  totalPedido: number;
  status?: string;
  itens: {
    id: number;
    variacaoProduto: VariacaoProduto & { produto?: Produto };
    quantidade: number;
    precoUnitario: number;
    subtotal: number;
  }[];
}

-- Seed compartilhado (CONVENCOES.md §8): 3 unidades de Curitiba, lotações das personas e 2 pacientes.
-- Nenhum registro de fila: eles são criados no fluxo da demonstração.

INSERT INTO unidades (id, nome, tipo, latitude, longitude) VALUES
    (N'upa-01', N'UPA Boa Vista', N'UPA', -25.3866, -49.2346),
    (N'upa-02', N'UPA Cajuru', N'UPA', -25.4486, -49.2183),
    (N'ubs-03', N'UBS Mãe Curitibana', N'UBS', -25.4372, -49.2700);

INSERT INTO lotacoes (id, usuario_id, unidade_id, perfil) VALUES
    (N'lot-1', N'usr-atendente-07', N'upa-01', N'ATENDENTE'),
    (N'lot-2', N'usr-atendente-07', N'upa-02', N'ATENDENTE'),
    (N'lot-3', N'usr-prof-03', N'upa-01', N'PROFISSIONAL'),
    (N'lot-4', N'usr-gestor-01', N'upa-01', N'GESTOR'),
    (N'lot-5', N'usr-gestor-01', N'upa-02', N'GESTOR'),
    (N'lot-6', N'usr-gestor-01', N'ubs-03', N'GESTOR');

INSERT INTO pacientes (id, nome, cpf, cartao_sus) VALUES
    (N'pac-101', N'João Pereira', N'11111111111', N'700000000000001'),
    (N'pac-102', N'Maria Souza', N'22222222222', N'700000000000002');
